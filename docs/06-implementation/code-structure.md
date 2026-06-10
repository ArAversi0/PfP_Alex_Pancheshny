# Структура кода

Проект организован как монорепозиторий с несколькими приложениями и общим модулем доменной логики.

```text
PfP/
  apps/
    backend/
    web/
    desktop/
  libs/
    game-rules/
  infra/
  contracts/
  docs/
```

## Backend

Backend находится в `apps/backend` и построен вокруг доменных модулей:

```text
apps/backend/src/main/java/com/pfp/companion/
  bootstrap/
  charactersheet/
  charactertransfer/
  content/
  identityaccess/
  notification/
  sharedkernel/
```

Внутри модулей используется разделение, близкое к PCMEF:

- `control` - REST-контроллеры, DTO, mapper-классы.
- `mediator` - прикладные сервисы, оркестрация и сценарии использования.
- `entity` - JPA-сущности и доменная логика.
- `foundation` - адаптеры хранения, безопасность, email и внешняя инфраструктура.
- `sharedkernel` - общие исключения, идентификаторы, типы.

Ключевые backend-сценарии:

- регистрация и вход пользователя;
- выдача JWT-токена;
- Google OAuth2 login;
- подтверждение email;
- CRUD персонажей;
- частичные изменения листа персонажа;
- импорт и экспорт JSON;
- управление контентом лора и справочника;
- админский просмотр пользователей и персонажей.

## Web

Web-клиент находится в `apps/web`:

```text
apps/web/src/
  app/
  assets/
  features/
  pages/
  shared/
```

Feature-модули:

- `features/auth` - авторизация, регистрация, профиль, восстановление пароля.
- `features/character-sheet` - архив персонажей, лист, игровые блоки, гостевое хранилище.
- `features/character-transfer` - импорт и экспорт JSON.
- `features/content` - Lore и Rule book.
- `features/admin` - панель администратора, пользователи, персонажи и редактор контента.

API-взаимодействие вынесено в `api`-файлы внутри feature-модулей. Общая настройка Axios и JWT-интерцепторов находится в `shared`/`api`.

## Desktop

Desktop-клиент находится в `apps/desktop`:

```text
apps/desktop/src/main/java/com/pfp/desktop/
  control/
  foundation/
  presentation/
  statemanagement/
  PfpDesktopApplication.java
  PfpDesktopLauncher.java
```

Назначение слоёв:

- `control` - навигация между окнами и сценариями приложения.
- `foundation/api` - HTTP-клиенты backend API, хранилище сессии, DTO режима аккаунта.
- `foundation/json` - локальное JSON-хранилище гостевого режима, импорт/экспорт.
- `foundation/content` - загрузка встроенного лора и справочника.
- `foundation/settings` - настройки окна, режимов отображения и горячих клавиш.
- `foundation/audio` - музыкальные темы и звуковые эффекты.
- `presentation` - JavaFX views и визуальная сборка экранов.
- `statemanagement` - область состояния приложения.

Для корректного запуска packaged-приложения используется `PfpDesktopLauncher`, который отделяет точку входа от JavaFX `Application`.

## Общий модуль игровых правил

`libs/game-rules` содержит общие правила PfP, расчёты характеристик, навыков, здоровья, валют и игровых структур. Вынесение правил в отдельный модуль снижает расхождение логики между backend, web и desktop.

## Инфраструктура

`infra/compose.yaml` поднимает локальные сервисы:

- PostgreSQL для backend-хранилища.
- Mailpit для dev-проверки email-сценариев.

## Документация и контракты

`docs` содержит проектную документацию, пользовательские инструкции, UI-описания и финальные материалы. `contracts` используется для фиксации контрактов и структуры обмена данными.
