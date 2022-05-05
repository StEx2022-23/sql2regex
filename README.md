<h1 align="center">
    sqltoregex
</h1>

<div align="center">
  
  [![LICENCE](https://img.shields.io/github/license/StEx2022-23/sql2regex.svg)](https://github.com/binkertpat/sql2regex)
  [![ISSUES](https://img.shields.io/github/issues/StEx2022-23/sql2regex.svg)](https://github.com/binkertpat/sql2regex)
  [![ISSUES](https://img.shields.io/github/issues-closed/StEx2022-23/sql2regex.svg)](https://github.com/binkertpat/sql2regex)
  [![PULL REQUESTS](https://img.shields.io/github/issues-pr/StEx2022-23/sql2regex.svg)](https://github.com/binkertpat/sql2regex)
  [![PULL REQUESTS](https://img.shields.io/github/issues-pr-closed/StEx2022-23/sql2regex.svg)](https://github.com/binkertpat/sql2regex)
  [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=StEx2022-23_sql2regex&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=StEx2022-23_sql2regex)
  [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=StEx2022-23_sql2regex&metric=bugs)](https://sonarcloud.io/summary/new_code?id=StEx2022-23_sql2regex)
  [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=StEx2022-23_sql2regex&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=StEx2022-23_sql2regex)
  [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=StEx2022-23_sql2regex&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=StEx2022-23_sql2regex)
  [![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=StEx2022-23_sql2regex&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=StEx2022-23_sql2regex)
  [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=StEx2022-23_sql2regex&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=StEx2022-23_sql2regex)
  [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=StEx2022-23_sql2regex&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=StEx2022-23_sql2regex)
  [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=StEx2022-23_sql2regex&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=StEx2022-23_sql2regex)
  [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=StEx2022-23_sql2regex&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=StEx2022-23_sql2regex)

</div>

# installation notes

## scss → css
hard compile:

<code>npm run scss</code>

compile changes on runtime:
<code>npm run watch</code>

## install/build application
<code>./mvnw clean install</code>

## start application
<code>./mvnw spring-boot:run </code>

## automatic deploy to heroku
<a href="https://sql2regex.herokuapp.com/"> 
  <img src="https://cdn.worldvectorlogo.com/logos/heroku-1.svg" height="30">
</a>

## REST-Api

### single request with command line

```cmd
curl -X POST http://localhost:8080/convert -H "Content-Type: application/json" -d "{\"sql\":\"SELECT * FROM table\"}"
```

### single request with python

```python
import requests

headers = {'Content-Type': 'application/json'}
r = requests.post('http://localhost:8080/convert', headers=headers, json={"sql":"SELECT * "})
print(r.json())
```

### multiple statements with python

```python
import requests
import json

headers = {'Content-Type': 'application/json'}
statementlist = [
    {"sql":"SELECT * FROM table"},
    {"sql":"SELECT * FROM table"},
    {"sql":"SELECT * FROM table"},
    {"sql":"SELECT * FROM table"},
    {"sql":"SELECT * FROM table"}
]
r = requests.post('http://localhost:8080/multiconvert', headers=headers, json=statementlist)
print(r.json())
```
## contributers
- Patrick Binkert, Technische Universität Dresden, student of teaching, 10th semester (physics and computer science)
- Maximilian Förster, Technische Universität Dresden, student of teaching, 10th semester (physics and computer science)

<br> 

<div align="center">
  
  [![Open Source Love](https://badges.frapsoft.com/os/v3/open-source-175x29.png?v=103)](https://github.com/ellerbrock/open-source-badges/)

</div>
