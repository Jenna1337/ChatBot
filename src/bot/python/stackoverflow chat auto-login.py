
import urllib
import urllib2
import cookielib
import re


username = ''
password = ''

roomid = 17

headers = {
    'User-Agent' : 'Mozilla/5.0 (X11; Linux i686; rv:17.0) Gecko/20100101 Firefox/17.0'
}

cj = cookielib.CookieJar()
opener = urllib2.build_opener(urllib2.HTTPCookieProcessor(cj))

request = urllib2.Request('http://stackoverflow.com/users/login', '', headers)
response = opener.open(request)


post_data = urllib.urlencode({
    'openid_identifier' : 'http://myopenid.com/',
})

request = urllib2.Request('http://stackoverflow.com/users/authenticate', post_data, headers)
response = opener.open(request)

response_text = response.read()

tid = re.search('name="tid"\s+value="([^"]+)"', response_text)
token = re.search('name="token"\s+value="([^"]+)"', response_text)
underscore = re.search('name="_"\s+value="([^"]+)"', response_text)

assert tid
assert token
assert underscore

post_data = urllib.urlencode({
    'tid': tid.group(1),
    'token': token.group(1),
    '_': underscore.group(1),
    'user_name': username,
    'password': password
})

request = urllib2.Request('https://www.myopenid.com/signin_submit', post_data, headers)
response = opener.open(request)


request = urllib2.Request('http://chat.stackoverflow.com/rooms/%d' % roomid, '', headers)
response = opener.open(request)

response_text = response.read()
fkey = re.search('name="fkey"\s+type="hidden"\s+value="([^"]+)"', response_text)

assert fkey

fkey = fkey.group(1)

print fkey


#post_data = urllib.urlencode({
#    'text': 'It works!',
#    'fkey': fkey
#})
#
#request = urllib2.Request('http://chat.stackoverflow.com/chats/%d/messages/new' % roomid, post_data, headers)
#response = opener.open(request)
#
#print response.read()
