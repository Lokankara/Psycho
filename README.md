```
taskkill /F /IM java.exe /T

./gradlew :rest:bootRun :ui:bootRun :kanban:bootRun :agile:bootRun --parallel 

./gradlew clean build

./gradlew.bat clean build --no-daemon 2>&1

./gradlew runAll --parallel

./gradlew :qa:e2eTest  --no-daemon

```

'rest' - quiz-api  
'ui' quiz-ui
'bdd' - e2e
'kanban' - dashboard-ui
'agile' - kanban-api


https://collective-unconscious.onrender.com