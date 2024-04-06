# paper-selection
师生论文双选系统


## redis存储
1. session存储 
   + key->`login:student:{userAccount}`
   + value->`{objectMapper.writeValueAsString(User)}`

2. 是否在时间段内登录字段
   + key->`UserLoginIsRunning`
   + value->`{boolean}`
