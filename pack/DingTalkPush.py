# author By Martin
# https://github.com/MartinxMax
''' TK  = DingTalk(True)
    TK.set_token('')
    TK.set_secret('')
    # TK.send_text('hello world')
    TK.send_pic(message='123',picture='https://image.3001.net/images/20230614/1686672054_648892b6df1d96ccda918.png',title='maptnh',link='https://www.freebuf.com/author/maptnh')
    '''
import requests
import time
import hmac
import hashlib
import base64
import urllib.parse
from datetime import datetime

class DingTalk(object):

    def __init__(self,echo=True):
        self.__token = None
        self.__secret = None
        self.__exho = echo


    def __build_sign(self):
        try:
            timestamp = str(round(time.time() * 1000))
            secret_enc = self.__secret.encode('utf-8')
            string_to_sign = '{}\n{}'.format(timestamp, self.__secret)
            string_to_sign_enc = string_to_sign.encode('utf-8')
            hmac_code = hmac.new(secret_enc, string_to_sign_enc, digestmod=hashlib.sha256).digest()
            sign = urllib.parse.quote_plus(base64.b64encode(hmac_code))
        except Exception as e:
            raise ValueError('[ERROR] You must set TOKEN and signature')
        else:
            return f'https://oapi.dingtalk.com/robot/send?access_token={self.__token}&timestamp={timestamp}&sign={sign}'

    def set_token(self,token):
        self.__token = token

    def set_secret(self,secret):
        self.__secret = secret


    def demo(func):
        def wrapper(self, *args, **kwargs):
            url = self.__build_sign()
            data = func(self, *args, **kwargs)  # 将实例对象作为第一个参数传递给被装饰的方法
            try:
                respon = requests.post(url, json=data, timeout=5)
            except Exception as e:
                raise ValueError('[ERROR] Unable to establish connection with DingTalk server')
            else:
                if self.__exho:
                    print(f"[{respon.status_code}]-{respon.url}>{respon.text}")
                return respon.status_code, respon.url, respon.text
        return wrapper

    @demo
    def send_text(self, message):
        textdemo = {
            "text": {
                "content": "{}"
            },
            "msgtype": "text"
        }
        textdemo['text']['content'] = textdemo['text']['content'].format(message) # 更新消息内容
        return textdemo

    @demo
    def send_pic(self,message=' ',title='@Martin',picture=' ',link=' '):
        picdemo = {
            "msgtype": "markdown",
            "markdown": {
                "title": "{} [{}]",
                "text": "\n![screenshot]({})\n{}\n\n--[More]({})\n"
            }
        }
        picdemo['markdown']['title'] = picdemo['markdown']['title'].format(title,str(datetime.now()))
        picdemo['markdown']['text'] = picdemo['markdown']['text'].format(picture,message,link)
        return picdemo