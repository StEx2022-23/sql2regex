import requests

headers = {'Content-Type': 'application/json'}

r = requests.post('http://sql2regex.herokuapp.com/api/settingstypes', headers=headers)
print(r.text)

# ["ALL","DEFAULT_SCHOOL","USER"]