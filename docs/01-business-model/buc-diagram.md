# BUC-диаграмма

```plantuml
@startuml
left to right direction
skinparam shadowing false

actor "Гость" as Guest
actor "Пользователь" as User
actor "Администратор" as Admin
actor "Платёжный сервис" as Pay

rectangle "PFP" {
  usecase "Регистрация" as UC1
  usecase "Авторизация" as UC2
  usecase "Создание персонажа" as UC3
  usecase "Редактирование листа персонажа" as UC4
  usecase "Управление инвентарем" as UC5
  usecase "Бросок костей" as UC6
  usecase "Импорт / экспорт JSON" as UC7
  usecase "Оформление подписки" as UC8
  usecase "Просмотр Книги Игрока" as UC9
  usecase "Просмотр Лора" as UC10
  usecase "Модерация контента" as UC11
  usecase "Управление пользователями" as UC12
}

Guest --> UC1
Guest --> UC10
Guest --> UC9

User --> UC2
User --> UC3
User --> UC4
User --> UC5
User --> UC6
User --> UC7
User --> UC8
User --> UC9
User --> UC10

Admin --> UC11
Admin --> UC12

UC4 .> UC3 : <<include>>
UC5 .> UC4 : <<include>>
UC8 .> UC2 : <<include>>
UC8 --> Pay
@enduml
```

---

## Описание BUC-диаграммы

BUC-диаграмма отражает ключевые бизнес-прецеденты системы и показывает, какие роли участвуют в работе с платформой PFP.

Гость может просматривать публичный контент и работать в локальном режиме desktop-версии. Зарегистрированный пользователь получает доступ к созданию и редактированию персонажей, управлению инвентарем, виртуальным броскам костей, импорту и экспорту JSON. Администратор отвечает за модерацию контента и управление пользователями. Прецедент «Оформление подписки» включает авторизацию и обращение к внешнему платёжному сервису.