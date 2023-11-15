import datetime
import socket
import json
import sys
sys.path.append(".")
from pack.S_Clustr_AES import S_Clustr_AES_CBC

configs = json.load(open('./config/Version.conf'))[sys.argv[0].split('.')[0]]

title = f'''
************************************************************************************
当前为测试环境,用于与服务端之间的通讯调试
Currently in a testing environment for communication debugging with the server
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
                @Мартин. S-Clustr(Shadow Cluster) Device [Simulation test] {configs['version']}'''


DEVICE_TYPES = {
    1: {"TYPE": "Arduino"},
    2: {"TYPE": "C51"},
    3: {"TYPE": "STM32"},
    4: {"TYPE": "PC"}
}

def Main():
    AESS = S_Clustr_AES_CBC()
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    device_type = int(input("[Enter the serial number of the simulated object]\r\n1.Arduino 2.C51 3.STM32 4.PC[encryption] >"))
    target = input("[Target](xxx.xxx.xxx.xxx:10000)>").split(':')
    if device_type==4:
        KEY = input("[KEY]>")
    CONFIG = json.load(open('./config/Client.conf'))
    device_info = DEVICE_TYPES.get(device_type)
    SERVER_ADDRESS = (target[0], int(target[1]))
    if not device_info:
        print("Invalid device type")
        exit()

    try:
        client_socket.connect(SERVER_ADDRESS)
        data = json.dumps(device_info)
        if device_type == 4:
            data = AESS.aes_cbc_encode(KEY, data)
        client_socket.send(data.encode('utf-8'))
        print(f'[{datetime.datetime.now().strftime("%Y-%m-%d %H:%M")}] Successfully connected to server:',target[0],target[1])
        while True:
            data = client_socket.recv(1024).decode('utf-8')
            if device_type == 4:
                data = AESS.aes_cbc_decode(KEY, data)
            if data == CONFIG[DEVICE_TYPES[device_type]["TYPE"]]["RUN"]:
                if device_type == 4:
                    client_socket.sendall(AESS.aes_cbc_encode(KEY, CONFIG[DEVICE_TYPES[device_type]["TYPE"]]["DEV_RUN_RECV"]).encode('utf-8'))
                else:
                    client_socket.sendall(CONFIG[DEVICE_TYPES[device_type]["TYPE"]]["DEV_RUN_RECV"].encode('utf-8'))
            elif data == CONFIG[DEVICE_TYPES[device_type]["TYPE"]]["STOP"]:
                if device_type == 4:
                    client_socket.sendall(AESS.aes_cbc_encode(KEY, CONFIG[DEVICE_TYPES[device_type]["TYPE"]]["DEV_STOP_RECV"]).encode('utf-8'))
                else:
                    client_socket.sendall(CONFIG[DEVICE_TYPES[device_type]["TYPE"]]["DEV_STOP_RECV"].encode('utf-8'))
            print(f'[{datetime.datetime.now().strftime("%Y-%m-%d %H:%M")}] received data:', data)

    except ConnectionRefusedError:
        print(f'[{datetime.datetime.now().strftime("%Y-%m-%d %H:%M")}] The server cannot connect!!!')
    except Exception as e:
        print(e)
    finally:
        client_socket.close()


if __name__ == '__main__':
    print(logo,title)
    Main()
