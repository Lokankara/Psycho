# JSON file store

CRUD for JSON files, saved into folder `\agile\src\main\resources\json\{}\filename.json`

Same Java port is exposed through Spring proxy `JsonProxyController`

- `GET /api/json/bugs` -> list of stored payloads
- `GET /api/json/task` -> list of stored payloads
- `GET /api/json/story` -> list of stored payloads
- `GET /api/json/<id>` -> file content
- `PUT /api/json/<id>` -> write JSON body, `201` created / `200` overwritten
- `DELETE api/json/<id>` -> `{"status":"deleted"}`

Config in `agile/src/main/resources/application.properties`:
