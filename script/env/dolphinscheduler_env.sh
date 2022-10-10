#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# JAVA_HOME, will use it to start DolphinScheduler server
export JAVA_HOME=${JAVA_HOME:-/Library/Java/JavaVirtualMachines/temurin-11.jdk/Contents/Home}

# Database related configuration, set database type, username and password
export DATABASE=${DATABASE:-mysql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true"
export SPRING_DATASOURCE_USERNAME=developer
export SPRING_DATASOURCE_PASSWORD=developer1qazXSW@

# DolphinScheduler server related configuration
export SPRING_CACHE_TYPE=${SPRING_CACHE_TYPE:-none}
# export SPRING_JACKSON_TIME_ZONE=${SPRING_JACKSON_TIME_ZONE:-UTC}
export SPRING_JACKSON_TIME_ZONE=${SPRING_JACKSON_TIME_ZONE:-Asia/Shanghai}
export MASTER_FETCH_COMMAND_NUM=${MASTER_FETCH_COMMAND_NUM:-10}

# Registry center configuration, determines the type and link of the registry center
export REGISTRY_TYPE=${REGISTRY_TYPE:-zookeeper}
export REGISTRY_ZOOKEEPER_CONNECT_STRING=${REGISTRY_ZOOKEEPER_CONNECT_STRING:-localhost:2181}

# Tasks related configurations, need to change the configuration if you use the related tasks.
export HADOOP_HOME=${HADOOP_HOME:-/Users/jiangqian/Documents/_AllDocMap/06_Software/hadoop-3.3.4}
export HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-/Users/jiangqian/Documents/_AllDocMap/06_Software/hadoop-3.3.4/etc/hadoop}
export SPARK_HOME1=${SPARK_HOME1:-/Users/jiangqian/Documents/_AllDocMap/06_Software/spark-3.3.0-bin-hadoop3}
export SPARK_HOME2=${SPARK_HOME2:-/Users/jiangqian/Documents/_AllDocMap/06_Software/spark-3.3.0-bin-hadoop3}
export PYTHON_HOME=${PYTHON_HOME:-/Users/jiangqian/Documents/_AllDocMap/06_Software/venv3/bin/python3}
export HIVE_HOME=${HIVE_HOME:-/Users/jiangqian/Documents/_AllDocMap/06_Software/apache-hive-3.1.2-bin}
export FLINK_HOME=${FLINK_HOME:-/Users/jiangqian/Documents/_AllDocMap/06_Software/flink-1.15.2}
export DATAX_HOME=${DATAX_HOME:-/Users/jiangqian/Documents/_AllDocMap/02_Project/github/DataX/target/datax/datax}

export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$SPARK_HOME2/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$PATH
