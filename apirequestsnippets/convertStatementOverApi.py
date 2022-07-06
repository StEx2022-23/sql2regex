import requests

headers = {'Content-Type': 'application/json'}

r = requests.post('http://sql2regex.herokuapp.com/api/convert', headers=headers, 
json={"sql":["SELECT col1, col2 FROM table", "INSERT INTO tab1 VALUES ('col1', 'col2')"], "settingsType":"ALL"})
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