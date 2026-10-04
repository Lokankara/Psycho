```
taskkill /F /IM java.exe /T

./gradlew :rest:bootRun :ui:bootRun :kanban:bootRun :agile:bootRun --parallel

./gradlew :bdd:bootRun 

./gradlew clean build

./gradlew bootRun

./gradlew.bat clean build --no-daemon 2>&1

./gradlew runAll --parallel

./gradlew :rest:bootRun :agile:bootRun :ui:bootRun :kanban:bootRun --parallel


```

'rest' - quiz-api  
'ui' quiz-ui
'bdd' - e2re
'kanban' - dashboard-ui
'agile' - kanban-api


https://collective-unconscious.onrender.com