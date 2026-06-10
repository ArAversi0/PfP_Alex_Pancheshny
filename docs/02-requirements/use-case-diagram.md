# Use-case диаграмма

```plantuml
@startuml
left to right direction

actor Guest as ROLE_GUEST
actor User as ROLE_USER
actor Admin as ROLE_ADMIN
actor "OAuth2 Provider" as OAuth

rectangle "PFP Companion System" {

usecase "UC-001\nРегистрация пользователя" as UC1
usecase "UC-002\nАутентификация" as UC2
usecase "UC-003\nСоздание персонажа" as UC3
usecase "UC-004\nРедактирование персонажа" as UC4
usecase "UC-005\nУправление инвентарём" as UC5
usecase "UC-006\nЭкипировка предмета" as UC6
usecase "UC-007\nПросмотр справочника правил" as UC7
usecase "UC-008\nБросок виртуальных костей" as UC8
usecase "UC-009\nЭкспорт персонажа" as UC9
usecase "UC-010\nИмпорт персонажа" as UC10
usecase "UC-011\nУправление контентом" as UC11
usecase "UC-012\nУправление пользователями" as UC12
usecase "UC-013\nOAuth2 авторизация" as UC13
usecase "UC-014\nПересчёт характеристик" as UC14
usecase "UC-015\nРасчёт overweight" as UC15
}

ROLE_GUEST --> UC3
ROLE_GUEST --> UC5
ROLE_GUEST --> UC8
ROLE_GUEST --> UC9
ROLE_GUEST --> UC10

ROLE_USER --> UC2
ROLE_USER --> UC3
ROLE_USER --> UC4
ROLE_USER --> UC5
ROLE_USER --> UC7
ROLE_USER --> UC8
ROLE_USER --> UC9
ROLE_USER --> UC10

ROLE_ADMIN --> UC11
ROLE_ADMIN --> UC12

OAuth --> UC13

UC6 .> UC14 : <<include>>
UC5 .> UC15 : <<include>>
UC2 .> UC13 : <<extend>>
UC4 .> UC14 : <<include>>
@enduml

```

## Описание

Диаграмма вариантов использования отражает ключевые сценарии взаимодействия пользователей с системой сопровождения персонажей PFP и демонстрирует переход от бизнес-процессов к системной функциональности. 
В качестве акторов выделены роли:

+ ["software_role","ROLE_GUEST","unauthorized user role"],
+ ["software_role","ROLE_USER","authorized user role"]
+ ["software_role","ROLE_ADMIN","administrator role"], 
+ А также внешняя система аутентификации ["software_system","OAuth2 Provider","external auth system"].

Основной функционал сосредоточен вокруг управления персонажем, инвентарём и игровыми механиками: создание и редактирование персонажа, работа с инвентарём и экипировкой, выполнение виртуальных бросков костей, импорт и экспорт данных. Администратор отвечает за управление пользователями и контентом системы.

Диаграмма также отражает внутреннюю логику системы через отношения <<include>> и <<extend>>. Связи <<include>> используются для обязательных вычислений (например, пересчёт характеристик и расчёт overweight), а <<extend>> — для расширения базовых сценариев, таких как авторизация через OAuth2.

Таким образом, диаграмма фиксирует структуру системных требований и показывает, каким образом пользователи взаимодействуют с системой в рамках ключевых бизнес-задач.