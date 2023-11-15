#!/usr/bin/python3
# @Мартин.
import datetime
import json
import sys
import base64

configs = json.load(open('./config/Version.conf'))[sys.argv[0].split('.')[0]]
version = f"@Мартин. S-Clustr(Shadow Cluster) Generate embedded device code {configs['version']}"

title = f'''
************************************************************************************
自动生成嵌入式设备的代码
Automatically generate code for embedded devices
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
                {version}
1.Arduino 2.ESP8266 3.AIR780E 4.AT89C51 5.STM32 6.Nets3e
'''

class Main():
    def __init__(self):
        self._CONFIG = json.load(open('./config/Client.conf'))
        self.__DEVICE = {1: "Arduino", 2: "ESP8266",3:"AIR780E",4:"C51",5:"STM32",6:"Nets3e"}
        types = self.__get_device_type()
        server = self.__get_server_address()
        server_run = self._CONFIG[self.__DEVICE[types]].get('RUN')
        server_runback = self._CONFIG[self.__DEVICE[types]].get('DEV_RUN_RECV')
        server_stop = self._CONFIG[self.__DEVICE[types]].get('STOP')
        server_stopback = self._CONFIG[self.__DEVICE[types]].get('DEV_STOP_RECV')
        if not all([server_run, server_runback, server_stop, server_stopback]):
            raise ValueError(f"[{datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}] Missing configuration values for {self.__DEVICE[types]} server.")
        if types == 1:
            choice = int(input("1.SIM900A 2.ENC28J60 > "))
            if choice == 1:
                self.__arduino('SIM900A',server[0], server[1], server_run, server_runback, server_stop, server_stopback)
            elif choice == 2:
                self.__arduino('ENC28J60',server[0], server[1], server_run, server_runback, server_stop, server_stopback)
            else:
                print("Exit...")
        elif types == 2:
            ssid,password = self.__ssid_password()
            if ssid and password:
                self.__esp8266(server[0], server[1], server_run, server_runback, server_stop, server_stopback,ssid,password)
            else:
                print("Exit...")
        elif types == 3:
            self.__air780e(server[0], server[1], server_run, server_runback, server_stop, server_stopback)
        elif types == 4:
            self.__c51(server[0], server[1], server_run, server_runback, server_stop, server_stopback)
        elif types == 5:
            self.__stm32(server[0], server[1], server_run, server_runback, server_stop, server_stopback)
        elif types == 6:
            nets3e_server = input("[Nets3e Server IP Address](http://xxx.xxx.xxx.xxx:10000)> ")
            s_clustr_key = input("[S-Clustr Server Devicie Key Server)> ")
            netse3_salt = input("[Nets3e Server Salt ]> ")

            self.__nets3e(server[0], server[1], nets3e_server,server_run, server_runback, server_stop, server_stopback,s_clustr_key,netse3_salt)

    def __get_device_type(self):
        while True:
            try:
                types = int(input("[Device Type (Number)]>"))

                if types in self.__DEVICE:
                    return types
                else:
                    print(f"[{datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}] Invalid device type. Please enter a number between 1 and 3.")
            except ValueError:
                print(f"[{datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}] Invalid input. Please enter a number.")

    def __get_server_address(self):
        while True:
            server = input("[S-Clustr Server IP Address](xxx.xxx.xxx.xxx:10000)> ").split(':')
            if len(server) == 2 and server[1].isdigit():
                return server
            else:
                print(f"[{datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}] Invalid input. Please enter the server IP address and port number in the format xxx.xxx.xxx.xxx:10000.")


    def __generate(self, types, ext,data):
        try:
            with open(f'./Device/Output/{types}.{ext}', 'w', encoding='utf-8') as f:
                f.write(data)
        except FileNotFoundError:
            print(f"[{datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}] Error: File not found: {types}.{ext}")
        except PermissionError:
            print(f"[{datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}] Error: Permission denied to write to file: {types}.{ext}")
        except UnicodeEncodeError:
            print(f"[{datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}]Error: Failed to encode data to utf-8")
        else:
            print(f"[{datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}] File [./Device/Output/{types}.{ext}] generated successfully")

    def __read_file(self,types,ext):
        data = ''
        try:
            with open(f'./Device/Source/{types}.{ext}', 'r', encoding='utf-8') as f:
                data = f.read()
        except FileNotFoundError:
            print(f"[{datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}] Error: File not found: {types}.{ext}")
        except PermissionError:
            print(f"[{datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}] Error: Permission denied to write to file: {types}.{ext}")
        except UnicodeEncodeError:
            print(f"[{datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}]Error: Failed to encode data to utf-8")
        else:
            return data


    def __arduino(self,type,ip,port,run,runback,stop,stopback):
        __arduino = self.__read_file('Arduino'+f'_{type}','source')
        data = __arduino.replace('@SERVER_IP',ip)
        data = data.replace('@SERVER_PORT',port)
        data = data.replace('@SERVER_RUN',run)
        data = data.replace('@RESPON_RUN',runback)
        data = data.replace('@SERVER_STOP',stop)
        data = data.replace('@RESPON_STOP',stopback)
        self.__generate('arduino/Arduino'+f'_{type}','ino',data)



    def __esp8266(self,ip,port,run,runback,stop,stopback,ssid,password):
        esp8266 = self.__read_file('ESP8266','source')
        data = esp8266.replace('@SERVER_IP',ip)
        data = data.replace('@SERVER_PORT',port)
        data = data.replace('@SERVER_RUN',run)
        data = data.replace('@RESPON_RUN',runback)
        data = data.replace('@SERVER_STOP',stop)
        data = data.replace('@RESPON_STOP',stopback)
        data = data.replace('@SSID',ssid)
        data = data.replace('@PASSWORD',password)
        self.__generate('esp8266/ESP8266','ino',data)


    def __ssid_password(self):
        ssid = input("[Wifi][SSID] > ")
        password = input("[Wifi][Password] > ")
        if len(ssid) >= 1 and len(password) >= 8:
            return ssid, password
        else:
            print("Invalid input. SSID must be at least 1 character long and password must be at least 8 characters long.")
            return False,False


    def __air780e(self,ip,port,run,runback,stop,stopback):
        __air780e = self.__read_file('AIR780E','source')
        data = __air780e.replace('@SERVER_IP',ip)
        data = data.replace('@SERVER_PORT',port)
        data = data.replace('@SERVER_RUN',run)
        data = data.replace('@RESPON_RUN',runback)
        data = data.replace('@SERVER_STOP',stop)
        data = data.replace('@RESPON_STOP',stopback)
        self.__generate('AIR780E/main','lua',data)


    def __c51(self,ip,port,run,runback,stop,stopback):
        __c51 = self.__read_file('C51','source')
        data = __c51.replace('@SERVER_IP',ip)
        data = data.replace('@SERVER_PORT',port)
        data = data.replace('@SERVER_RUN',run)
        data = data.replace('@RESPON_RUN',runback)
        data = data.replace('@SERVER_STOP',stop)
        data = data.replace('@RESPON_STOP',stopback)
        self.__generate('C51/main','c',data)

    def __stm32(self,ip,port,run,runback,stop,stopback):
        __stm32 = self.__read_file('stm32','source')
        data = __stm32.replace('@SERVER_IP',ip)
        data = data.replace('@SERVER_PORT',port)
        data = data.replace('@SERVER_RUN',run)
        data = data.replace('@RESPON_RUN',runback)
        data = data.replace('@SERVER_STOP',stop)
        data = data.replace('@RESPON_STOP',stopback)
        self.__generate('STM32/main','c',data)

    def __nets3e(self,ip,port,nets3e_server,run,runback,stop,stopback,s_clustr_key,netse3_salt):
        __nets3e = self.__read_file('Nets3e','source')
        data = __nets3e.replace('@SERVER_IP',ip)
        data = data.replace('@SERVER_PORT',port)
        data = data.replace('@SERVER_RUN',run)
        data = data.replace('@NETS3E_SERVER',base64.b64encode(nets3e_server.encode('utf-8')).decode('utf-8'))
        data = data.replace('@RESPON_RUN',runback)
        data = data.replace('@SERVER_STOP',stop)
        data = data.replace('@RESPON_STOP',stopback)
        data = data.replace('@S_CLUSTR_KEY',s_clustr_key)
        data = data.replace('@NET_SALT',netse3_salt)
        self.__generate('Nets3e/main','py',data)

if __name__ == '__main__':
    print(logo,title)
    Main()
