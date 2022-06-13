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

### example request over command line

```cmd
curl -X POST http://localhost:8080/api/convert -H "Content-Type: application/json" -d "{\"sql\":[\"SELECT * FROM table\"],\"settingsType\":\"ALL\"}"
```

### requests with python and examples

```python
import requests

headers = {'Content-Type': 'application/json'}

r = requests.post('http://localhost:8080/api/docs', headers=headers)
print(r.text)
# Available endpoints:
#    - /convert
#    - /settingstypes
#    - /settingsoptions
#    - /specificsettingsoption

r = requests.post('http://localhost:8080/api/convert', headers=headers, json={"sql":["SELECT col1, col2 FROM table", "INSERT INTO tab1 VALUES ('col1', 'col2')"], "settingsType":"ALL"})
print(r.text)
# {
#    "sql":[
#       "SELECT col1, col2 FROM table",
#       "INSERT INTO tab1 VALUES ('col1', 'col2')"
#    ],
#    "settingsType":"ALL",
#    "regex":[
#       "^(?:SELECT|ELECT|SLECT|SEECT|SELCT|SELET|SELEC)\\s+(?:(?:col1|ol1|cl1|co1|col)\\s*(\\s*(?:(?:ALIAS|LIAS|AIAS|ALAS|ALIS|ALIA)|(?:AS|S|A))\\s+.*)?\\s*,\\s*(?:col2|ol2|cl2|co2|col)\\s*(\\s*(?:(?:ALIAS|LIAS|AIAS|ALAS|ALIS|ALIA)|(?:AS|S|A))\\s+.*)?|(?:col2|ol2|cl2|co2|col)\\s*(\\s*(?:(?:ALIAS|LIAS|AIAS|ALAS|ALIS|ALIA)|(?:AS|S|A))\\s+.*)?\\s*,\\s*(?:col1|ol1|cl1|co1|col)\\s*(\\s*(?:(?:ALIAS|LIAS|AIAS|ALAS|ALIS|ALIA)|(?:AS|S|A))\\s+.*)?)\\s+(?:FROM|ROM|FOM|FRM|FRO)\\s+(?:table|able|tble|tale|tabe|tabl)(\\s*(?:(?:ALIAS|LIAS|AIAS|ALAS|ALIS|ALIA)|(?:AS|S|A))?\\s+.*)?$",
#       "^(?:INSERT|NSERT|ISERT|INERT|INSRT|INSET|INSER)\\s+(?:INTO|NTO|ITO|INO|INT)\\s+(?:tab1|ab1|tb1|ta1|tab)\\s+(?:VALUE|ALUE|VLUE|VAUE|VALE|VALU)S?\\s+\\(\\s*(?:['`\"]?(?:col1|ol1|cl1|co1|col)['`\"]?\\s*,\\s*['`\"]?(?:col2|ol2|cl2|co2|col)['`\"]?|['`\"]?(?:col2|ol2|cl2|co2|col)['`\"]?\\s*,\\s*['`\"]?(?:col1|ol1|cl1|co1|col)['`\"]?)\\s*\\)\\s*$"
#    ]
# }

r = requests.post('http://localhost:8080/api/settingstypes', headers=headers)
print(r.text)
# ["ALL","DEFAULT_SCHOOL","USER"]

r = requests.post('http://localhost:8080/api/settingsoptions', headers=headers)
print(r.text)
# ["COLUMNNAMESPELLING","KEYWORDSPELLING","TABLENAMESPELLING","COLUMNNAMEORDER","TABLENAMEORDER","INDEXCOLUMNNAMEORDER","INDEXCOLUMNNAMESPELLING","DATESYNONYMS","DATETIMESYNONYMS","TIMESYNONYMS","AGGREGATEFUNCTIONLANG","DATATYPESYNONYMS","GROUPBYELEMENTORDER","INSERTINTOVALUESORDER","OTHERSYNONYMS","DEFAULT"]

r = requests.post('http://localhost:8080/api/specificsettingsoption', headers=headers, data="ALL")
print(r.text)
# ["COLUMNNAMESPELLING","KEYWORDSPELLING","TABLENAMESPELLING","COLUMNNAMEORDER","TABLENAMEORDER","INDEXCOLUMNNAMEORDER","INDEXCOLUMNNAMESPELLING","DATESYNONYMS","DATETIMESYNONYMS","TIMESYNONYMS","AGGREGATEFUNCTIONLANG","DATATYPESYNONYMS","GROUPBYELEMENTORDER","INSERTINTOVALUESORDER","OTHERSYNONYMS"]

```

## documentation

### javadoc
The current javadoc is available here: https://stex2022-23.github.io/index.html

### top-level-architecture
coming soon

## contributers
- Patrick Binkert, Technische Universität Dresden, student of teaching, 10th semester (physics and computer science)
- Maximilian Förster, Technische Universität Dresden, student of teaching, 10th semester (physics and computer science)

<br> 

<div align="center">
  
  [![Open Source Love](https://badges.frapsoft.com/os/v3/open-source-175x29.png?v=103)](https://github.com/ellerbrock/open-source-badges/)

</div>
