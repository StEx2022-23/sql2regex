import requests

headers = {'Content-Type': 'application/json'}

r = requests.post('http://sql2regex.herokuapp.com/api/docs', headers=headers)
print(r.text)

# Available endpoints:
#    - /convert
#    - /settingstypes
#    - /settingsoptions
#    - /specificsettingsoption