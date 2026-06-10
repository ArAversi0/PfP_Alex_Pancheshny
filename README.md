# PfP Companion System

**PfP Companion System** - enterprise-приложение для ведения персонажей, правил и лора собственной настольной ролевой системы **PfP (Pain for Pleasure)**. Проект объединяет веб-клиент, desktop-клиент, backend API, PostgreSQL-хранилище, админский контур и общий модуль игровых правил.

Главная цель проекта - дать игрокам и мастеру единое рабочее пространство: создавать листы персонажей, редактировать игровые параметры, вести инвентарь и заклинания, импортировать и экспортировать JSON, читать справочник правил и лор, а также синхронизировать персонажей между веб-версией и desktop-приложением через один аккаунт.

## Возможности

- Регистрация, вход по email/паролю, подтверждение email через dev-почту Mailpit.
- Вход через Google OAuth2.
- JWT-аутентификация и разграничение ролей пользователя и администратора.
- Веб-архив персонажей с лимитом 100 листов на аккаунт.
- Desktop-режим гостя с локальным JSON-хранилищем и лимитом 30 листов.
- Desktop-режим аккаунта с синхронизацией персонажей через backend API.
- Полноценный лист персонажа PfP: портрет, базовая информация, характеристики, навыки, здоровье по частям тела, экипировка, инвентарь, заклинания, заметки и дополнительные сведения.
- Drag-and-drop инвентаря, создание предмета через пустую ячейку, экипировка, снятие экипировки, продажа торговых предметов.
- Импорт и экспорт персонажей в JSON в веб-версии и desktop-версии.
- Read-only справочник правил и лора в desktop-приложении.
- Редактирование лора и справочника правил через web admin.
- Админская панель для управления пользователями, просмотра персонажей пользователей и редактирования контента.
- Desktop-настройки: разрешение окна, оконный/полноэкранный режим, горячие клавиши, музыка и звуки.
- Desktop-сборка в self-contained app image через `jpackage`; подготовлена задача для Windows installer.

## Архитектура

Проект оформлен как монорепозиторий:

```text
apps/
  backend/   Spring Boot backend API
  web/       React + TypeScript + Vite web client
  desktop/   JavaFX desktop client
libs/
  game-rules/ shared PfP rules and calculations
infra/
  compose.yaml PostgreSQL and Mailpit for local development
docs/
  project documentation
contracts/
  API and data contracts
```

Backend построен по модульной структуре PCMEF: `control`, `mediator`, `entity`, `foundation`, `sharedkernel`. Основные домены: `charactersheet`, `charactertransfer`, `content`, `identityaccess`, `notification`.

Web-клиент использует feature-based структуру: `auth`, `character-sheet`, `character-transfer`, `content`, `admin`. Desktop-клиент использует JavaFX и разделён на `control`, `foundation`, `presentation`, `statemanagement`.

## Стек технологий

Backend:

- Java 17
- Spring Boot 3
- Spring Web, Validation, Security
- Spring Data JPA
- PostgreSQL
- Flyway
- JWT / OAuth2 Client / OAuth2 Resource Server
- Spring Mail
- JUnit 5, Mockito, Testcontainers
- JaCoCo

Frontend:

- React 18
- TypeScript
- Vite
- React Router
- React Hook Form
- Axios
- ESLint

Desktop:

- Java 17
- JavaFX
- Gradle Application Plugin
- `jpackage`
- локальное JSON-хранилище для гостевого режима
- встроенные ресурсы лора, справочника, музыки, звуков и изображений листа

Infrastructure:

- Docker Compose
- PostgreSQL
- Mailpit

## Локальный запуск

### Инфраструктура

```powershell
docker compose -f infra/compose.yaml up -d
```

PostgreSQL доступен на `localhost:5432`, Mailpit UI - на `http://localhost:8025`.

### Backend

```powershell
$env:JAVA_HOME="C:\Users\user\.jdks\corretto-23.0.2"
.\gradlew.bat :apps:backend:bootRun
```

Backend запускается на `http://localhost:8080`.

### Web

```powershell
cd apps\web
npm install
npm run dev -- --host 127.0.0.1
```

Web-клиент открывается на `http://localhost:5173`.

### Desktop

```powershell
$env:JAVA_HOME="C:\Users\user\.jdks\corretto-23.0.2"
.\gradlew.bat :apps:desktop:run
```

Для режима аккаунта desktop-клиенту нужен запущенный backend. Гостевой режим работает локально без backend.

## Сборка и проверка

Backend tests + JaCoCo:

```powershell
$env:JAVA_HOME="C:\Users\user\.jdks\corretto-23.0.2"
.\gradlew.bat :apps:backend:test --console=plain
```

HTML-отчёт JaCoCo:

```text
apps/backend/build/reports/jacoco/test/html/index.html
```

Desktop tests:

```powershell
$env:JAVA_HOME="C:\Users\user\.jdks\corretto-23.0.2"
.\gradlew.bat :apps:desktop:test --console=plain
```

Web build:

```powershell
cd apps\web
npm run build
```

Desktop app image:

```powershell
$env:JAVA_HOME="C:\Users\user\.jdks\corretto-23.0.2"
.\gradlew.bat :apps:desktop:packageDesktopAppImage --console=plain
```

Готовое приложение создаётся в:

```text
apps/desktop/build/jpackage/image/PfP Companion/
```

Windows installer:

```powershell
$env:JAVA_HOME="C:\Users\user\.jdks\corretto-23.0.2"
.\gradlew.bat :apps:desktop:packageDesktopInstaller --console=plain
```

Для `.exe` installer на Windows требуется установленный WiX Toolset. Без WiX можно использовать self-contained app image.

## Статистика разработки

Для итоговой демонстрации проекта в этот раздел нужно вставить скриншоты из GitHub Insights или аналогичного инструмента:

- Commit Activity - график активности коммитов.
- Punch Card - тепловая карта распределения коммитов по дням недели и времени.

Рекомендуемый порядок получения скриншотов:

1. Создать удалённый репозиторий на GitHub.
2. Привязать локальный репозиторий к удалённому:

```powershell
git remote add origin <URL_РЕПОЗИТОРИЯ>
git push -u origin main
```

3. Открыть вкладку `Insights`.
4. Сделать скриншоты `Commit Activity` и `Punch Card`.
5. Добавить изображения в отчёт или презентацию проекта.

## Текущее состояние

Веб-часть и desktop-часть доведены до MVP+ уровня: реализованы основные пользовательские сценарии, синхронизация персонажей аккаунта, локальный гостевой режим, дополнительный контент в режиме только для чтения, админский контур, сборка и проверка тестами. Для промышленной эксплуатации остаются задачи по промышленной email-доставке, удобному назначению администраторов, CI/CD и выпуску подписанного desktop-установщика.
