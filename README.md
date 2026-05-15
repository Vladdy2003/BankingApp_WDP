# BankingApp

Aplicație bancară completă construită ca proiect de an, demonstrând utilizarea a **13 Design Patterns** (5 Creational, 4 Structural, 4 Behavioral).

> **Țara:** Republica Moldova — moneda implicită este **MDL** (leul moldovenesc). IBAN-urile respectă formatul moldovenesc: `MD` + 2 cifre control + 2 litere cod bancă + 18 cifre (24 caractere total).

---

## Cuprins

1. [Prezentare Generală](#1-prezentare-generală)
2. [Tehnologii Folosite](#2-tehnologii-folosite)
3. [Arhitectura Proiectului](#3-arhitectura-proiectului)
4. [Design Patterns Implementate](#4-design-patterns-implementate)
5. [Backend — .NET Web API](#5-backend--net-web-api)
6. [Aplicație Mobilă — Android](#6-aplicație-mobilă--android)
7. [Baza de Date](#8-baza-de-date)
8. [API Reference](#9-api-reference)
9. [Cum Rulezi Proiectul](#10-cum-rulezi-proiectul)
10. [Structura Folderelor](#11-structura-folderelor)

---

## 1. Prezentare Generală

BankingApp este un ecosistem format din trei componente:

| Componentă | Tehnologie | Port |
|---|---|---|
| **Backend API** | .NET 10 Web API | `https://localhost:7016` |
| **Aplicație Mobilă** | Android (Kotlin + Jetpack Compose) | — |

**Funcționalități principale:**
- Înregistrare și autentificare utilizatori cu JWT + Refresh Token
- Gestionarea conturilor bancare: Curent, Economii, Business
- Tranzacții: depozit, retragere, transfer între conturi (cu comision)
- Emitere și gestionare carduri: Debit, Credit, Prepaid
- Sistem de notificări in-app cu filtrare și paginare
- Rapoarte financiare: extras lunar, sumar anual, cheltuieli pe categorii, venituri vs cheltuieli
- Export extrase în format PDF și CSV
- Panou de administrare cu audit logs și management conturi

---

## 2. Tehnologii Folosite

### Backend
| Tehnologie | Versiune | Rol |
|---|---|---|
| .NET | 10.0 | Runtime + Web API |
| ASP.NET Core Identity | 10.0.6 | Autentificare utilizatori |
| Entity Framework Core | 10.0.6 | ORM + migrații |
| SQL Server (LocalDB) | — | Baza de date |
| JWT Bearer | 10.0.6 | Token-uri de autentificare |
| BCrypt.Net-Next | 4.0.3 | Hashing CVV card |
| QuestPDF | 2024.10.4 | Generare PDF extrase de cont |
| Swagger / OpenAPI | 10.1.7 | Documentație API interactivă |

### Aplicație Mobilă Android
| Tehnologie | Versiune | Rol |
|---|---|---|
| Kotlin | 2.2.10 | Limbaj de programare |
| Jetpack Compose BOM | 2026.02.01 | UI declarativ |
| Material3 | — | Design system |
| Navigation Compose | 2.8.9 | Navigare între ecrane |
| Retrofit | 2.11.0 | Networking HTTP |
| OkHttp | 4.12.0 | HTTP client |
| Gson | 2.10.1 | Serializare JSON |
| DataStore Preferences | 1.1.4 | Stocare locală (token JWT) |
| ViewModel + StateFlow | 2.8.7 | State management (MVVM) |


---

## 3. Arhitectura Proiectului

```
┌────────────────────────┐    
│   Android App          │    
│   (Kotlin + Compose)   │    
│   MVVM + Repository    │    
└───────────┬────────────┘    
            │ HTTPS/REST     
            │                 
            ▼                 
┌───────────────────────────────────────────────────────┐
│                  .NET 10 Web API                      │
│                  (port 7016 HTTPS)                    │
│                                                       │
│   AuthController   AccountsController                 │
│   CardsController  TransactionsController             │
│   NotificationsController  ReportsController          │
│   AdminController                                     │
│              │                                        │
│   BankingFacade ◄──── Facade Pattern                  │
│   ┌────────────┬───────────────┐                      │
│   ▼            ▼               ▼                      │
│   AccountService  CardService  TransactionService     │
│        │                            │                 │
│   AccountServiceProxy ◄── Proxy    Chain of Resp.     │
│                                    Command Pattern    │
│   Observer Pattern ──► NotificationService            │
│                                                       │
│   Repository Layer                                    │
│   Entity Framework Core                               │
└────────────────────────┬──────────────────────────────┘
                         │
                    SQL Server
```

### Pattern Arhitectural — Backend
- **Layered Architecture**: Controllers → Services → Repositories → DbContext
- **DI Container**: înregistrare manuală în `Program.cs` (~35 de înregistrări), fără framework extern
- **Namespace rădăcină**: `BankingApp`

### Pattern Arhitectural — Android
- **MVVM**: ViewModel + StateFlow + Repository
- **Single Activity**: `MainActivity` → `AppNavGraph` → Composable screens
- **Manual DI**: Repository instanțiat direct în ViewModel (fără Hilt)

---

## 4. Design Patterns Implementate

### Creational Patterns (5/5)

| # | Pattern | Clasă principală | Descriere |
|---|---|---|---|
| 1 | **Singleton** | `LoggerService`, `ConfigurationManager` | Instanță unică per aplicație, înregistrate în DI cu lifetime Singleton |
| 2 | **Factory Method** | `AccountFactory`, `IAccountFactory` | Creează tipul corect de cont (Curent/Economii/Business) în funcție de parametri |
| 3 | **Abstract Factory** | `NotificationFactory`, `INotificationFactory` | Produce `EmailNotification` sau `SmsNotification`; variantă `MockNotificationFactory` pentru teste |
| 4 | **Builder** | `TransactionBuilder`, `ITransactionBuilder` | Construiește tranzacții complexe pas cu pas (fluent API); `Build()` aruncă excepție la câmpuri lipsă |
| 5 | **Prototype** | `CardTemplate` | Clonează template-uri predefinite de card (`StandardDebitTemplate`, `PremiumCreditTemplate`); `CardFactory` personalizează clona |

### Structural Patterns (4/4)

| # | Pattern | Clasă principală | Descriere |
|---|---|---|---|
| 6 | **Facade** | `BankingFacade`, `IBankingFacade` | Interfață simplificată peste AccountService + TransactionService + CardService; metode high-level ca `GetAccountSummary`, `ProcessCompleteTransfer` |
| 7 | **Decorator** | `TransactionDecorator` | Înlănțuiește decoratori: `LoggingDecorator` → `FeeDecorator` → `NotificationDecorator` → `BasicProcessor` |
| 8 | **Proxy** | `AccountServiceProxy` | Wrappează `AccountService`; adaugă verificare permisiuni, caching în memorie și logging automat |
| 9 | **Adapter** | `FixerIoAdapter`, `OpenExchangeRatesAdapter` | Adaptează API-uri externe de curs valutar la interfața internă `IExchangeRateProvider` |

### Behavioral Patterns (4/4)

| # | Pattern | Clasă principală | Descriere |
|---|---|---|---|
| 10 | **Observer** | `TransactionEventPublisher`, `ITransactionObserver` | `BasicTransactionProcessor` publică eveniment; observatori: `EmailNotificationObserver`, `InAppNotificationObserver`, `AuditLogObserver` |
| 11 | **Strategy** | `IInterestCalculationStrategy` | Algoritmi diferiți de dobândă: `SimpleInterestStrategy`, `CompoundInterestStrategy`, `NoInterestStrategy`; injectat în `InterestService` |
| 12 | **Command** | `ITransactionCommand` | `DepositCommand`, `WithdrawalCommand`, `TransferCommand` cu `Execute()` și `Undo()`; `TransactionCommandInvoker` menține istoricul |
| 13 | **Chain of Responsibility** | `TransactionValidationChain` | Validatori în lanț: `AccountStatusValidator` → `BalanceValidator` → `DailyLimitValidator` → `FraudDetectionValidator` |

> **State Pattern** (bonus): `AccountStateContext` cu stările `ActiveAccountState`, `SuspendedAccountState`, `InactiveAccountState`, `ClosedAccountState` — integrat în `AccountService` pentru tranziții valide și în `AccountStatusValidator` pentru mesaje contextuale.

**Total: 5 Creational + 4 Structural + 4 Behavioral = 13 Design Patterns**

---

## 5. Backend — .NET Web API

### Modele EF Core (TPH — Table Per Hierarchy)

```
ApplicationUser : IdentityUser
  + FirstName, LastName, Address, DateOfBirth, CNP
  + RefreshToken, RefreshTokenExpiry

Account (abstract, discriminator pe Type)
  ├── CurrentAccount   + OverdraftLimit
  ├── SavingsAccount   + InterestRate
  └── BusinessAccount  + CompanyName

Transaction
  FromAccountId?, ToAccountId?, Amount, Currency
  Type (Deposit|Withdrawal|Transfer|Payment)
  Status (Pending|Completed|Failed|Cancelled)

Card
  AccountId, MaskedCardNumber, ExpiryDate, CVVHash
  Type (Debit|Credit|Prepaid)
  Status (Active|Blocked|Expired|Pending)
  DailyLimit, MonthlyLimit

Notification
  UserId, Title, Message
  Type (Transaction|Security|Marketing|System)
  IsRead

AuditLog
  UserId, Action, EntityType, EntityId
  OldValues, NewValues, IpAddress, UserAgent, Timestamp
```

### Configurare (`appsettings.json`)

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=(localdb)\\mssqllocaldb;Database=BankingAppDb;..."
  },
  "JwtSettings": {
    "SecretKey": "...",
    "Issuer": "BankingApp",
    "Audience": "BankingAppUsers",
    "ExpirationMinutes": 60,
    "RefreshTokenExpirationDays": 7
  },
  "BankingLimits": {
    "DailyTransactionLimit": 10000,
    "MaxTransferAmount": 50000,
    "FraudDetectionThreshold": 5000
  }
}
```

### Servicii înregistrate în DI (~35 înregistrări)

Principalele servicii:

| Serviciu | Lifetime | Rol |
|---|---|---|
| `AppDbContext` | Scoped | ORM context |
| `AuthService` | Scoped | JWT generation, register/login |
| `AccountService` / `AccountServiceProxy` | Scoped | CRUD conturi |
| `TransactionService` | Scoped | Procesare tranzacții |
| `CardService` | Scoped | Management carduri |
| `NotificationService` | Scoped | Notificări in-app |
| `ReportService` | Scoped | Rapoarte financiare |
| `AuditService` | Scoped | Audit log |
| `BankingFacade` | Scoped | Facade principal |
| `LoggerService` | Singleton | Logging global |
| `ConfigurationManager` | Singleton | Setări globale |

---

## 6. Aplicație Mobilă — Android

### Configurare

| Parametru | Valoare |
|---|---|
| Namespace | `com.example.bankingapp` |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 (Android 15) |
| Kotlin | 2.2.10 |
| AGP | 9.2.0 |
| Backend URL (emulator) | `https://10.0.2.2:7016/` |

### Ecrane Implementate

#### Autentificare (C.1)
| ID | Ecran | Descriere |
|---|---|---|
| C.1.1 | **SplashScreen** | Logo animat pe fundal obsidian; verifică token JWT → redirecționează |
| C.1.2 | **LoginScreen** | Email + parolă; validare locală; JWT salvat în DataStore |
| C.1.3 | **RegisterScreen** | 3 pași: date personale → contact → parolă; indicator progres animat |

#### Principal (C.2–C.7)
| ID | Ecran | Descriere |
|---|---|---|
| C.2 | **DashboardScreen** | Sold conturi (HorizontalPager), carduri, grafic cheltuieli 6 luni (Canvas), tranzacții recente |
| C.3 | **NotificationsScreen** | Listă paginată, filtrare pe tip, swipe-to-delete, mark as read |
| C.4.1 | **AccountsListScreen** | Lista conturilor cu sold și IBAN mascat; creare cont nou (BottomSheet) |
| C.4.2 | **AccountDetailScreen** | Header gradient cu sold hero, tranzacții infinite scroll, editare cont, închidere cont |
| C.5.1 | **CardsListScreen** | Carduri vizuale cu gradient obsidian; blocare/deblocare inline; creare card |
| C.5.2 | **CardDetailScreen** | Detalii card, limite cu progress bar, blocare/deblocare, închidere card |
| C.6.1 | **TransactionsListScreen** | Tranzacții grupate pe date (sticky headers), filtrare, paginare, SpeedDial FAB |
| C.6.2–4 | **Transaction Sheets** | BottomSheet pentru Depozit / Retragere / Transfer (comision: min(max(sumă×0.5%, 1 MDL), 50 MDL)) |
| C.7 | **SettingsScreen** | Profil utilizator, toggle temă dark/light, deconectare |
| C.7.1 | **ProfileScreen** | Vizualizare și editare date personale (stocare locală via DataStore) |
| C.7.2 | **ReportsScreen** | Extras lunar (export PDF/CSV), sumar anual (grafic bare Canvas), cheltuieli (donut chart), venituri vs cheltuieli (bar chart) |

### Arhitectura MVVM

```kotlin
// ViewModel → Repository → RetrofitClient → .NET API
class XxxViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = XxxRepository()
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(XxxUiState())
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()
}

// RetrofitClient — singleton cu AuthInterceptor
// setAuthToken(token) apelat în init al fiecărui ViewModel autentificat
```

### Navigare

```
Splash → Login / Main
Login  → Main (popUpTo Login inclusive) / Register
Register → Login (cu email prefilled)
Main (BottomNav 5 tab-uri):
  Tab 1: Dashboard → Notifications
  Tab 2: AccountsList → AccountDetail → TransactionSheets
  Tab 3: CardsList → CardDetail → AccountDetail
  Tab 4: TransactionsList → TransactionSheets
  Tab 5: Settings → Profile / Reports
```

### Design System

**Culori (paleta "Institutional Warmth"):**

| Token | Light | Dark | Utilizare |
|---|---|---|---|
| `background` | `#F2EFE9` | `#141210` | Fundal pagini |
| `surface` | `#FFFFFF` | `#1E1C18` | Carduri |
| `primary` (BaGold) | `#B8965A` | `#C9A96A` | Accent principal |
| `BaSuccess` | `#3A7D5C` | `#4D9E74` | Credite, stări pozitive |
| `BaDanger` | `#B84040` | `#D45C5C` | Debite, erori |
| `BaObsidian` | `#141210` | `#141210` | Header-uri hero, carduri |

**Tipografie:**
- **DM Serif Display** — titluri și solduri hero
- **DM Sans** — corp text, butoane, etichete
- **DM Mono** — IBAN-uri și numere de card

---

## 7. Baza de Date

**SQL Server LocalDB** — baza de date `BankingAppDb`

### Diagrama relațiilor principale

```
ApplicationUser (Identity)
    │
    ├──< Account (TPH: Current / Savings / Business)
    │         │
    │         ├──< Transaction (FromAccount / ToAccount)
    │         └──< Card
    │
    └──< Notification
    └──< AuditLog
```

### Migrații EF Core

```bash
dotnet ef migrations add <NumeMigrație>
dotnet ef database update
```

### Indecși și constrângeri cheie

- Index unic pe `Account.IBAN`
- FK `Transaction → FromAccount`: Restrict (nu se poate șterge contul dacă are tranzacții)
- FK `Transaction → ToAccount`: Restrict
- FK `Card → Account`: Cascade
- FK `Notification → User`: Cascade

---

## 8. API Reference

### Autentificare

| Metodă | Endpoint | Descriere |
|---|---|---|
| POST | `/api/auth/register` | Înregistrare utilizator nou |
| POST | `/api/auth/login` | Autentificare, returnează JWT + RefreshToken |
| POST | `/api/auth/refresh-token` | Reîmprospătare token JWT |

### Conturi

| Metodă | Endpoint | Descriere |
|---|---|---|
| GET | `/api/accounts` | Listează conturile utilizatorului autentificat |
| GET | `/api/accounts/{id}` | Detalii cont |
| POST | `/api/accounts` | Creează cont nou (type: 0=Curent, 1=Economii, 2=Business) |
| PUT | `/api/accounts/{id}` | Actualizează date cont |
| DELETE | `/api/accounts/{id}` | Închide cont (soft delete) |
| PUT | `/api/accounts/{id}/suspend` | Suspendă cont |
| PUT | `/api/accounts/{id}/activate` | Reactivează cont |
| GET | `/api/accounts/{id}/balance` | Sold curent |
| GET | `/api/accounts/{id}/transactions` | Tranzacțiile unui cont (paginare: `?page=1&pageSize=20`) |

### Tranzacții

| Metodă | Endpoint | Descriere |
|---|---|---|
| GET | `/api/transactions` | Istoricul tranzacțiilor (`?type&status&from&to&page&pageSize`) |
| POST | `/api/transactions/deposit` | Depozit |
| POST | `/api/transactions/withdraw` | Retragere |
| POST | `/api/transactions/transfer` | Transfer (cu comision automat) |

### Carduri

| Metodă | Endpoint | Descriere |
|---|---|---|
| GET | `/api/cards` | Listează cardurile utilizatorului |
| GET | `/api/cards/{id}` | Detalii card |
| POST | `/api/cards` | Emite card nou (type: 0=Debit, 1=Credit, 2=Prepaid) |
| PUT | `/api/cards/{id}/block` | Blochează card |
| PUT | `/api/cards/{id}/unblock` | Deblochează card |
| PUT | `/api/cards/{id}/limits` | Modifică limite zilnice/lunare |
| DELETE | `/api/cards/{id}` | Anulează card |
| POST | `/api/cards/{id}/validate-payment` | Validează o plată |

### Notificări

| Metodă | Endpoint | Descriere |
|---|---|---|
| GET | `/api/notifications` | Lista notificărilor (`?page&pageSize`) |
| GET | `/api/notifications/unread-count` | Numărul de notificări necitite |
| PUT | `/api/notifications/{id}/read` | Marchează ca citită |
| PUT | `/api/notifications/read-all` | Marchează toate ca citite |
| DELETE | `/api/notifications/{id}` | Șterge notificare |

### Rapoarte

| Metodă | Endpoint | Descriere |
|---|---|---|
| GET | `/api/reports/monthly-statement` | Extras lunar (`?accountId&year&month`) |
| GET | `/api/reports/annual-summary` | Sumar anual (`?accountId&year`) |
| GET | `/api/reports/spending` | Cheltuieli pe categorii (`?accountId&from&to`) |
| GET | `/api/reports/income-expenses` | Venituri vs cheltuieli (`?accountId&from&to`) |
| GET | `/api/reports/export` | Export PDF/CSV (`?accountId&format&year&month`) |

### Admin

| Metodă | Endpoint | Descriere |
|---|---|---|
| GET | `/api/admin/audit-logs` | Audit log (`?page&pageSize&userId&action&entityType&from&to`) |

> Toate endpoint-urile (cu excepția Auth) necesită header: `Authorization: Bearer <access_token>`

---

## 9. Cum Rulezi Proiectul

### Cerințe preliminare

- .NET SDK 10.0 (preview)
- SQL Server sau SQL Server LocalDB
- Android Studio (pentru aplicația mobilă) + SDK 36
- Emulator Android API 24+ sau dispozitiv fizic

### Backend (.NET Web API)

```bash
cd BankingApp_WDP/BankingApp_WDP

# Actualizează connection string în appsettings.json dacă e necesar

# Aplică migrările EF Core
dotnet ef database update

# Pornește serverul
dotnet run
# API disponibil la: https://localhost:7016
# Swagger UI:        https://localhost:7016/swagger
```

### Aplicație Mobilă (Android)

1. Deschide folderul `BankingApp_WDP/BankingApp_WDP.APP` în Android Studio
2. Asigură-te că backend-ul rulează pe mașina locală
3. Backend URL în emulator: `https://10.0.2.2:7016/` (deja configurat în `RetrofitClient.kt`)
4. Rulează aplicația pe emulator sau dispozitiv fizic (Min API 24)

> **SSL în DEBUG:** Emulatorului i se permite certificatul self-signed al .NET dev server printr-un `X509TrustManager` permisiv, configurat doar în build-ul de debug.

---

## 11. Structura Folderelor

```
BankingApp/
│
├── BankingApp_WDP/
│   │
│   ├── BankingApp_WDP/                    # Proiect .NET 10 Web API
│   │   ├── Controllers/
│   │   │   ├── AuthController.cs
│   │   │   ├── AccountsController.cs
│   │   │   ├── TransactionsController.cs
│   │   │   ├── CardsController.cs
│   │   │   ├── NotificationsController.cs
│   │   │   ├── ReportsController.cs
│   │   │   └── AdminController.cs
│   │   │
│   │   ├── Models/                        # Entități EF Core
│   │   │   ├── ApplicationUser.cs
│   │   │   ├── Account.cs
│   │   │   ├── CurrentAccount.cs
│   │   │   ├── SavingsAccount.cs
│   │   │   ├── BusinessAccount.cs
│   │   │   ├── Transaction.cs
│   │   │   ├── Card.cs
│   │   │   ├── Notification.cs
│   │   │   └── AuditLog.cs
│   │   │
│   │   ├── Patterns/
│   │   │   ├── Creational/
│   │   │   │   ├── Singleton/             # LoggerService, ConfigManager
│   │   │   │   ├── FactoryMethod/         # AccountFactory
│   │   │   │   ├── AbstractFactory/       # NotificationFactory
│   │   │   │   ├── Builder/               # TransactionBuilder
│   │   │   │   └── Prototype/             # CardTemplate
│   │   │   ├── Structural/
│   │   │   │   ├── Facade/                # BankingFacade
│   │   │   │   ├── Decorator/             # TransactionDecorator
│   │   │   │   ├── Proxy/                 # AccountServiceProxy
│   │   │   │   └── Adapter/               # ExchangeRateAdapters
│   │   │   └── Behavioral/
│   │   │       ├── Observer/              # TransactionEventPublisher
│   │   │       ├── Strategy/              # InterestCalculationStrategy
│   │   │       ├── Command/               # TransactionCommand
│   │   │       ├── ChainOfResponsibility/ # TransactionValidationChain
│   │   │       └── State/                 # AccountStateContext
│   │   │
│   │   ├── Services/
│   │   │   ├── AuthService.cs
│   │   │   ├── AccountService.cs
│   │   │   ├── TransactionService.cs
│   │   │   ├── CardService.cs
│   │   │   ├── NotificationService.cs
│   │   │   ├── ReportService.cs
│   │   │   └── AuditService.cs
│   │   │
│   │   ├── DTOs/
│   │   │   ├── Auth/
│   │   │   ├── Accounts/
│   │   │   ├── Cards/
│   │   │   ├── Transactions/
│   │   │   ├── Notifications/
│   │   │   └── Reports/
│   │   │
│   │   ├── Data/
│   │   │   ├── AppDbContext.cs
│   │   │   └── Migrations/
│   │   │
│   │   ├── appsettings.json
│   │   └── Program.cs
│   │
│   └── BankingApp_WDP.APP/                # Proiect Android (Kotlin)
│       └── app/src/main/java/com/example/bankingapp/
│           ├── MainActivity.kt
│           ├── data/
│           │   ├── api/                   # Retrofit API interfaces
│           │   ├── local/                 # TokenManager, ThemePreferences
│           │   ├── model/                 # DTOs Gson
│           │   ├── network/               # RetrofitClient
│           │   └── repository/            # Repository classes
│           ├── navigation/
│           │   ├── Screen.kt
│           │   ├── BottomNavItem.kt
│           │   └── AppNavGraph.kt
│           └── ui/
│               ├── components/            # BaButton, BaInput
│               ├── theme/                 # Color, Type, Theme
│               └── screens/
│                   ├── splash/
│                   ├── login/
│                   ├── register/
│                   ├── main/              # MainScreen (BottomNav)
│                   ├── dashboard/
│                   ├── accounts/
│                   ├── cards/
│                   ├── transactions/
│                   ├── notifications/
│                   └── settings/          # Settings, Profile, Reports
│
└── README.md
```

---

## Rezumat Design Patterns

| # | Pattern | Categorie | Clasă Principală |
|---|---|---|---|
| 1 | Singleton | Creational | `LoggerService`, `ConfigurationManager` |
| 2 | Factory Method | Creational | `AccountFactory` |
| 3 | Abstract Factory | Creational | `NotificationFactory` |
| 4 | Builder | Creational | `TransactionBuilder` |
| 5 | Prototype | Creational | `CardTemplate` |
| 6 | Facade | Structural | `BankingFacade` |
| 7 | Decorator | Structural | `TransactionDecorator` |
| 8 | Proxy | Structural | `AccountServiceProxy` |
| 9 | Adapter | Structural | `FixerIoAdapter`, `OpenExchangeRatesAdapter` |
| 10 | Observer | Behavioral | `TransactionEventPublisher` |
| 11 | Strategy | Behavioral | `InterestCalculationStrategy` |
| 12 | Command | Behavioral | `TransactionCommand` |
| 13 | Chain of Responsibility | Behavioral | `TransactionValidationChain` |

**Total: 5 Creational + 4 Structural + 4 Behavioral = 13 Design Patterns**
