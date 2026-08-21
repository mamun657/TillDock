import json, os, urllib.request, urllib.error

BASE = 'http://localhost:8080'

def post(path, body, headers=None):
    data = json.dumps(body).encode('utf-8')
    h = {'Content-Type': 'application/json'}
    if headers: h.update(headers)
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

parts = [chr(c) for c in [97,117,100,114,101,121,46,111,119,110,101,114,64,116,105,108,108,100,111,99,107,46,116,101,115,116]]
email = ''.join(parts)

def load(p):
    return open(p, 'rb').read().decode('utf-8-sig').strip()

wp = load(os.environ['SYSROOT'] + '\\wp.bin')
rp = load(os.environ['SYSROOT'] + '\\rp.bin')

print('email_len =', len(email))
print('wp_len =', len(wp))
print('rp_len =', len(rp))

print('--- 1) login WRONG ---')
s, b = post('/api/auth/login', {'email': email, 'password': wp})
print('STATUS =', s)
print('BODY =', b)

print('--- 2) login RIGHT ---')
s, b = post('/api/auth/login', {'email': email, 'password': rp})
print('STATUS =', s)
print('BODY_LEN =', len(b))

print('--- 3) /me no token ---')
s, b = get('/api/auth/me')
print('STATUS =', s)
print('BODY =', b)

print('--- 4) /me bogus token ---')
s, b = get('/api/auth/me', {'Authorization': 'Bearer xxx'})
print('STATUS =', s)
print('BODY =', b)
