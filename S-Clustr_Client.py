import cmd
import time
import socket
import json
import sys
sys.path.append(".")
from pack.S_Clustr_AES import S_Clustr_AES_CBC

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

class S_Clustr(cmd.Cmd):
    intro = 'Welcome to S-Clustr console. Type [options][help/?] to list commands.\n'
    prompt = f'S-Clustr({configs["version"]})> '


    def __init__(self):
        super().__init__()
        self.PAYLOAD = PAYLOAD()
        self.options = {
            "rhost": {"value":"","description":"The target host"},
            "rport": {"value":"9999","description":"The target port (TCP)"},
            "id": {"value":"","description":"Device ID [0-n/0 represents specifying all]"},
            "pwr": {"value":"","description":"Device behavior (run[1]/stop[2]/Query device status[3])"},
            "key": {"value":"","description":"Server Key"}
        }


    def do_set(self, arg):
        """Set an option value. Usage: set <option> <value>"""
        try:
            option, value = arg.split()
        except ValueError:
            print("[-] Invalid syntax. Usage: set <option> <value>")
            return
        else:
            if option in self.options:
                self.options[option]['value'] = value
                print(f'[*] {option} => {value}')
            else:
                print(f'[-] Unknown variable: {option}')


    def do_run(self,arg):
        print("[*] Connecting to the server...")
        self.PAYLOAD.run(self.options)


    def do_options(self, arg):
        """List all available options and their current values."""
        table =  "| Name           | Current Setting | Required | Description       \n"
        table += "|:--------------:|:---------------:|:--------:|:-----------------\n"
        for key in self.options:
            name = f"{key:<14}"
            setting = f"{self.options[key]['value'] if self.options[key]['value'] else ' ':<15}"
            required = f"{'no' if self.options[key]['value'] else 'yes':<8}"
            description = f"{self.options[key]['description']:<20}"
            table += f"| {name} | {setting} | {required} | {description}\n"
        table += "|:--------------:|:---------------:|:--------:|:-----------------\n"
        print(table)


    def do_exit(self, arg):
        """Exit the program. Usage: exit"""
        return True


class PAYLOAD():


    def __init__(self):
        self.__aes=S_Clustr_AES_CBC()
        self.__BEHAVIORS = {1: 'RUN', 2: 'STOP', 3: 'Query State'}


    def run(self,info):
        if self.__check_params_complete(info):
            id = int(info['id']['value'])
            pwr = int(info['pwr']['value'])
            key = info['key']['value']
            rhost = info['rhost']['value']
            rport = int(info['rport']['value'])
            if pwr >0 and pwr < 4:
                if self.__check_params(id,pwr,key,rhost,rport):
                    currentTime = int(time.time())
                    signature = self.__aes.aes_cbc_encode(key, str(currentTime))
                    payload = self.__build_payload(currentTime,id,pwr,signature)
                    self.__send_hex_string(payload,id, pwr,rhost,rport,key)
            else:
                print(f"[-] The status parameter is not within the valid range![1-3]")


    def __check_params_complete(self,info):
        for key in ['id', 'pwr', 'key', 'rhost', 'rport']:
            if key not in info or not info[key].get('value'):
                print(f"[-] Parameter '{key}' is missing or incomplete!")
                return False
        return True

    def __check_params(self,id, pwr, key, rhost, rport):
        if not isinstance(id, int):
            print("[-] The id parameter must be an integer!")
            return False
        if not isinstance(pwr, int) or pwr < 1 or pwr > 3:
            print("[-] The pwr parameter must be an integer between 1 and 3!")
            return False
        if not isinstance(key, str):
            print("[-] The key parameter must be a string!")
            return False
        if not isinstance(rhost, str):
            print("[-] The rhost parameter must be a string!")
            return False
        if not isinstance(rport, int):
            print("[-] The rport parameter must be an integer!")
            return False
        return True

    def __send_hex_string(self,hex_string,id,pwr,rhost,rport,key):
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.settimeout(3)
        client_socket.connect((rhost, int(rport)))
        print(f"[*] Attempting to authenticate to the server [{rhost}:{rport}]")
        client_socket.sendall(hex_string.encode('utf-8'))
        while True:
            try:
                result = self.__aes.aes_cbc_decode(key,client_socket.recv(2048).decode('utf-8'))
                if not result:
                    break
                if pwr == 3 :
                    result = json.loads(result)
                if pwr == 3 and id == 0:
                    self.__display(result)
                elif pwr == 3 and id > 0:
                    print(f"[*] Device ID:{result['device_id']} Type:{result['device_type']} Device State:{'Stopped' if  result['device_state'] == 0 else 'Runing'} Device Network:{'Disconnected' if result['device_connect_state'] == 0 else 'Connected'}")
                else:
                    print(f"[*] Control Device ID:[{id}] Action:[{self.__BEHAVIORS[pwr]}]")
                    print("[+] "+result)
            except socket.timeout:
                break
            except Exception as e:
                break
        client_socket.close()


    def __build_payload(self,time,id,state,signature):
        return f"{time:08x} {id:04x} {state:02x} {signature}"


    def __display(self, jsond):
        table = "|   Device ID   |  Device Type  | Device State | Device Network |\n"
        table += "|:-------------:|:-------------:|:-------------:|:---------------:|\n"
        for device_id, device_state in jsond["device_state"].items():
            device_connect_state = jsond["device_connect_state"][device_id]
            device_type = jsond["device_type"][device_id]
            device_id_formatted = f"{device_id:^14}"
            device_type_formatted = f"{device_type if device_type else 'None':^14}"
            device_state_formatted = f"{'Stopped' if device_state == 0 else 'Running':^14}"
            device_network_formatted = f"{'Disconnected' if device_connect_state == 0 else 'Connected':^16}"
            table += f"| {device_id_formatted} | {device_type_formatted} | {device_state_formatted} | {device_network_formatted} |\n"
        table += "|:-------------:|:-------------:|:-------------:|:---------------:|\n"
        print(table)

if __name__ == '__main__':
    print(logo)
    print(title)
    S_Clustr().cmdloop()
