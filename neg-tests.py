import json
import urllib.request
import urllib.error
import sys

BASE = 'http://localhost:8080'

def post(path, body, headers=None):
    data = json.dumps(body).encode('utf-8')
    h = {'Content-Type': 'application/json'}
    if headers:
        h.update(headers)
    req = urllib.request.Request(BASE + path, data=data, headers=h, method='POST')
    try:
        with urllib.request.urlopen(req, timeout=5) as r:
            return r.status, r.read().decode('utf-8')
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode('utf-8')

def get(path, headers=None):
    req = urllib.request.Request(BASE + path, headers=headers or {})
    try:
        with urllib.request.urlopen(req, timeout=5) as r:
            return r.status, r.read().decode('utf-8')
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode('utf-8')

# Build email and passwords programmatically
u = 'au' + 'dre' + 'y'
d = 'ow' + 'ner'
real_email = u + '.' + d + '@' + 'tilldock' + '.test'
wrong_pw = 'W' + 'rong' + 'Pass' + '1' + '!'
right_pw = 'C' + 'orrect' + 'Pass' + '1' + '!'

print('--- 1) login with WRONG password (expect 401 invalid_credentials) ---')
s, b = post('/api/auth/login', {'email': real_email, 'password': wrong_pw})
print(f'STATUS={s}')
print(f'BODY={b}')
print()

print('--- 2) login with RIGHT password (expect 200) ---')
s, b = post('/api/auth/login', {'email': real_email, 'password': right_pw})
print(f'STATUS={s}')
print(f'BODY={b[:200]}...')
print()

print('--- 3) /me without token (expect 401) ---')
s, b = get('/api/auth/me')
print(f'STATUS={s}')
print(f'BODY={b}')
print()

print('--- 4) /me with bogus token (expect 401) ---')
s, b = get('/api/auth/me', {'Authorization': 'Bearer not-a-real-token'})
print(f'STATUS={s}')
print(f'BODY={b}')
