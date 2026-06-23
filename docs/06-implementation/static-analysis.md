# Отчет статического анализа

Дата проверки: 23.06.2026.

Отчет фиксирует проверки, используемые в проекте PfP Companion System для контроля качества кода, типовой корректности и архитектурной структуры. Проект не использует отдельный сервер SonarQube, поэтому результаты оформлены как локальный статический анализ по доступным инструментам Gradle, TypeScript, ESLint и структурной проверке PCMEF.

## Проверяемые области

| Область | Инструмент / артефакт | Результат |
| --- | --- | --- |
| Java backend | Gradle compilation, JUnit 5, Spring Boot Test | Код компилируется, backend test-results содержит 60 успешных тестов |
| Java desktop | Gradle compilation, JUnit 5 | Desktop test-results содержит 9 успешных тестов |
| Web TypeScript | `npm run build` (`tsc -b && vite build`) | Проверяется типовая корректность, импорты и production bundle |
| Web lint | `npm run lint` (ESLint) | Проверяются React Hooks, TypeScript и общие правила JavaScript/TypeScript |
| Архитектура PCMEF | Структура пакетов и документация `docs/03-architecture` | Backend разделен на `control`, `mediator`, `entity`, `foundation`; desktop содержит `presentation`, `control`, `foundation` |
| Покрытие | JaCoCo HTML report | Instruction coverage - 63%, branch coverage - 53% |
| Интеграционные проверки | Testcontainers/PostgreSQL | Проверяется persistence-адаптер JPA для персонажей |

## Команды проверки

```powershell
$env:JAVA_HOME="C:\Users\user\.jdks\corretto-23.0.2"
.\gradlew.bat :apps:backend:test --console=plain
.\gradlew.bat :apps:desktop:test --console=plain
```

```powershell
cd apps\web
npm run lint
npm run build
```

## Архитектурные замечания

Критических нарушений слоевой структуры PCMEF по проектной документации и структуре исходного кода не выявлено. Контроллеры backend расположены в `control`, use case координируются сервисами `mediator`, доменные модели находятся в `entity`, а JPA-представления, репозитории и mapper-адаптеры вынесены в `foundation/persistence`.

## Вывод

Проект проходит доступные локальные проверки качества: компиляцию Java-модулей, JUnit-тесты, TypeScript build, ESLint-проверку web-клиента и JaCoCo-контроль покрытия. Для промышленной эксплуатации можно дополнительно подключить SonarQube или Checkstyle в CI, но для учебного проекта статический контроль оформлен и подтвержден существующими артефактами.
