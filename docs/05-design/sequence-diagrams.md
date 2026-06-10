# Описание диаграмм последовательностей

## UC-03: Создание персонажа

Таблица 1 – Описание сценария UC-03

| Атрибут | Значение |
|--------|---------|
| Название | Создание персонажа |
| Актор | User |
| Описание | Пользователь создаёт нового персонажа с базовыми характеристиками |
| Слои | P, C, M, E, F |

Рисунок 1 – Диаграмма последовательности UC-03

```plantuml
@startuml
actor User
boundary UI
control CharacterController
control CharacterService
entity Character
entity CharacterStats
database CharacterRepository

User -> UI : заполнение формы
UI -> CharacterController : createCharacter(dto)

CharacterController -> CharacterController : validate(dto)

CharacterController -> CharacterService : createCharacter(dto)

CharacterService -> Character : new Character()
CharacterService -> CharacterStats : init base stats
CharacterService -> Character : set stats

CharacterService -> CharacterRepository : save(character)

CharacterRepository --> CharacterService : saved entity
CharacterService --> CharacterController : CharacterDTO
CharacterController --> UI : response

@enduml
```

---

## Описание

+ Пользователь инициирует создание персонажа через пользовательский интерфейс.
+ Контроллер принимает запрос и выполняет первичную валидацию DTO.
+ После успешной валидации запрос передаётся в слой Mediator (сервис).
+ Сервис формирует агрегат Character, включая инициализацию связанной сущности CharacterStats.
+ Выполняется установка базовых характеристик персонажа.
+ Сформированный агрегат сохраняется через репозиторий (слой Foundation).
+ После сохранения возвращается DTO с результатом операции.
+ Ответ передаётся обратно в UI.

### Особенности реализации

+ строго соблюдается направление зависимостей PCMEF;
+ бизнес-логика полностью изолирована в сервисе;
+ контроллер не содержит доменной логики;
+ используется агрегатный подход (Character как aggregate root);
+ обеспечивается расширяемость за счёт возможности добавления Builder/Factory в дальнейшем.

---

## UC-07: Управление инвентарём и экипировкой

Таблица 2 – Описание сценария UC-07

| Атрибут | Значение |
|--------|---------|
| Название | Управление инвентарём и экипировкой |
| Актор | User |
| Описание | Пользователь добавляет предмет в инвентарь и экипирует его в соответствующий слот |
| Слои | P, C, M, E, F |

Рисунок 2 – Диаграмма последовательности UC-07

```plantuml
@startuml
actor User
boundary UI
control InventoryController
control InventoryService
entity Inventory
entity Item
entity EquipmentSlot
database InventoryRepository

User -> UI : выбрать предмет и слот
UI -> InventoryController : equipItem(dto)

InventoryController -> InventoryController : validate(dto)

InventoryController -> InventoryService : equipItem(dto)

InventoryService -> InventoryRepository : findByCharacterId()

InventoryRepository --> InventoryService : Inventory

InventoryService -> Inventory : equip(item, slot)

Inventory -> EquipmentSlot : assign(item)

InventoryService -> InventoryRepository : save(inventory)

InventoryRepository --> InventoryService
InventoryService --> InventoryController : InventoryDTO
InventoryController --> UI : response

@enduml
```

---

### Описание

+ Пользователь инициирует экипировку предмета через UI, выбирая предмет и слот.
+ Контроллер принимает DTO и выполняет валидацию входных данных.
+ После валидации запрос передаётся в сервисный слой (Mediator).
+ Сервис загружает текущий инвентарь персонажа через репозиторий.
+ В доменной модели вызывается операция экипировки (equip).
+ Сущность Inventory делегирует назначение предмета соответствующему EquipmentSlot.
+ Выполняются проверки:
    * совместимость предмета и слота;
    * доступность слота;
    * ограничения по весу (overweight).
+ После изменения состояния агрегата выполняется сохранение через репозиторий.
+ Возвращается обновлённый DTO инвентаря.

### Особенности реализации

+ Inventory выступает агрегатным корнем для управления предметами;
+ логика экипировки инкапсулирована в доменной модели (Entity слой);
+ сервис выполняет координацию и транзакционное управление;
+ соблюдается строгая изоляция слоёв (Controller не содержит бизнес-логики);
+ реализована возможность расширения логики экипировки (например, эффекты предметов);
+ учитываются игровые ограничения (body zones, overweight, тип экипировки).

---

## UC-11: Импорт и экспорт персонажа

Таблица 3 – Описание сценария UC-11

| Атрибут | Значение |
|--------|---------|
| Название | Импорт/экспорт персонажа |
| Актор | User |
| Описание | Пользователь экспортирует персонажа в файл или импортирует его из файла |
| Слои | P, C, M, E, F |

Рисунок 3 – Диаграмма последовательности UC-11

```plantuml
@startuml
actor User
boundary UI
control CharacterController
control ImportExportService
entity Character
database CharacterRepository
database FileStorage

== Export ==

User -> UI : экспорт персонажа
UI -> CharacterController : exportCharacter(id)

CharacterController -> ImportExportService : exportCharacter(id)

ImportExportService -> CharacterRepository : findById(id)
CharacterRepository --> ImportExportService : Character

ImportExportService -> Character : prepareForExport()

ImportExportService -> FileStorage : writeJSON(character)

FileStorage --> ImportExportService : file
ImportExportService --> CharacterController : file
CharacterController --> UI : download

== Import ==

User -> UI : импорт файла
UI -> CharacterController : importCharacter(file)

CharacterController -> ImportExportService : importCharacter(file)

ImportExportService -> FileStorage : readJSON(file)
FileStorage --> ImportExportService : data

ImportExportService -> Character : reconstruct(data)

ImportExportService -> CharacterRepository : save(character)

CharacterRepository --> ImportExportService
ImportExportService --> CharacterController : CharacterDTO
CharacterController --> UI : response

@enduml
```

---

### Описание

+ Пользователь инициирует экспорт или импорт персонажа через UI.
+ Контроллер принимает запрос и передаёт его в специализированный сервис импорта/экспорта.
+ При экспорте:
    * происходит загрузка агрегата Character из базы данных;
    * выполняется подготовка данных к сериализации;
    * данные записываются в JSON-файл через слой хранения.
+ При импорте:
    * файл считывается из хранилища;
    * выполняется десериализация в доменные объекты;
    * агрегат Character восстанавливается;
    * объект сохраняется в базе данных.
+ После завершения операции возвращается результат в виде DTO или файла.

### Особенности реализации

+ импорт/экспорт вынесен в отдельный сервис (Single Responsibility);
+ используется сериализация JSON для обеспечения offline-совместимости;
+ Character рассматривается как агрегатный корень при восстановлении;
+ обеспечивается изоляция между persistence и файловым хранилищем;
+ возможна реализация через паттерн Factory Method для создания объектов из JSON;
+ поддерживается offline режим desktop-клиента.
