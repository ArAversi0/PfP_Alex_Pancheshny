# Тестирование

В проекте используются автоматические и ручные проверки. Основной акцент сделан на backend-домене, persistence-слое, JSON-переносе персонажей, desktop-хранилище и сборке клиентов.

## Backend

Backend проверяется через JUnit 5, Spring Boot Test, Mockito и Testcontainers. Основные группы тестов:

- entity-тесты для доменной логики листа персонажа;
- mapper-тесты для преобразования DTO и сущностей;
- service-тесты для сценариев создания, изменения, импорта и экспорта персонажей;
- security-тесты для JWT и поведения, связанного с OAuth2;
- admin-тесты для пользователей и контента;
- persistence integration tests через PostgreSQL/Testcontainers;
- email-тесты SMTP-адаптера.

Команда запуска:

```powershell
$env:JAVA_HOME="C:\Users\user\.jdks\corretto-23.0.2"
.\gradlew.bat :apps:backend:test --console=plain
```

## JaCoCo

Для backend включён JaCoCo. После запуска тестов формируется HTML-отчёт:

```text
apps/backend/build/reports/jacoco/test/html/index.html
```

Актуальный результат после завершения desktop-этапа:

- Instruction coverage: **63%**
- Branch coverage: **53%**

Требование покрытия не менее 40% выполняется.

## Desktop

Desktop-тесты проверяют фундаментальные сценарии JavaFX-приложения без необходимости ручного запуска UI:

- хранение сессии аккаунта;
- локальное JSON-хранилище гостевого режима;
- сериализация и десериализация листа;
- загрузка встроенного Lore/Rule book;
- структуры листа персонажа аккаунта.

Команда запуска:

```powershell
$env:JAVA_HOME="C:\Users\user\.jdks\corretto-23.0.2"
.\gradlew.bat :apps:desktop:test --console=plain
```

## Web

Для web-клиента выполняются TypeScript build и ESLint-проверка:

```powershell
cd apps\web
npm run build
npm run lint
```

Сборка проверяет маршруты, компоненты, типы и корректность production bundle.

## Ручные проверки

Ручной регрессионный набор включает:

- регистрацию через email и проверку письма в Mailpit;
- вход через email/пароль;
- вход через Google OAuth2;
- создание, открытие, удаление персонажа;
- импорт и экспорт JSON в веб-версии;
- импорт и экспорт JSON в desktop-гостевом режиме и режиме аккаунта;
- синхронизацию персонажей аккаунта между веб-версией и desktop;
- редактирование листа, инвентаря, экипировки, заклинаний и дополнительных сведений;
- проверку drag-and-drop инвентаря;
- продажу trade-предметов;
- проверку Lore и Rule book;
- проверку admin dashboard, users, characters и content;
- проверку desktop-настроек, горячих клавиш, музыки и звуков;
- запуск packaged desktop app image.

## Проверка сборки desktop

Self-contained desktop app image собирается командой:

```powershell
$env:JAVA_HOME="C:\Users\user\.jdks\corretto-23.0.2"
.\gradlew.bat :apps:desktop:packageDesktopAppImage --console=plain
```

После сборки проверяется запуск:

```text
apps/desktop/build/jpackage/image/PfP Companion/PfP Companion.exe
```
