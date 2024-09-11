# 使用说明
## 会遇到的问题：
1.缺失license
虽然已经用maven集成了gurobi的API包，但是该API的调用需要验证本机是否有gurobi的证书。
若是电脑上没有安装gurobi,会报错
```angular2html
    com.gurobi.gurobi.GRBException: No Gurobi license found (user administrator,xxxx)
```

解决方法一：（本质是为了获取gurobi.lic文件）
1. 安装gurobi
    [gurobi安装教程：](https://support.gurobi.com/hc/en-us/articles/14799677517585-Getting-Started-with-Gurobi-Optimizer)
    [gurobi optimizer下载连接：](https://www.gurobi.com/downloads/gurobi-software/)
2. 申请license
   自己想办法。获取到你自己的grbgetkey
3. 安装该软件并输入你的grbgetkey后获取得一个gurobi.lic文件，该文件即为所缺
4. 选做：如果gurobi.lic文件放在你自定义得文件路径下,需要把该文件（特指.lic文件）添加到环境变量
   以windows为例：set the environment variable GRB_LICENSE_FILE to point to this file
解决方法二：（本质是为了获取gurobi.lic文件）
1. WSL证书得部分也能直接获得一个证书文件。该解决方法有待验证。需要注意是是该方法获取得license授权时间会偏短。
