# entity-repository-generator

sebuah plugin yang dapat digunakan untuk generate entity dan repository dari database mysql (hanya mysql). dapat mendukung hingga many to many relation

Note:
- apllication properties / yaml harus ada ini 

spring.datasource.url=(url example)
spring.datasource.username=(username example)
spring.datasource.password=(password example)
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

#generator dir
generator.base-package=(package exampl)

generator.entity-dir=(entity dir destination example)
generator.repo-dir=(repo dir destination example)

#hibernate 
generator.hibernate.database=(database name yang mau di hibernate)

generator.hibernate.output=hibernate.reveng.xml
generator.hibernate.tables=(table yang mau hibernate example : customer,sales,user,etc)