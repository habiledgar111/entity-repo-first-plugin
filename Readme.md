# entity-repository-generator

sebuah plugin yang dapat digunakan untuk generate entity dan repository dari database mysql (hanya mysql). dapat mendukung hingga many to many relation


## cara Penggunaan : 

1. java harus >= 17 / springboot >= 3
2. application.properties / application.yaml harus ada berisi sebagai berikut :
> - spring.datasource.url=(url example)
> - spring.datasource.username=(username example)
> - spring.datasource.password=(password example)
> - spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
> - generator.base-package=(package exampl)
> - generator.entity-dir=(entity dir destination example)
> - generator.repo-dir=(repo dir destination example)
> - generator.hibernate.database=(database name yang mau di hibernate)
> - generator.hibernate.output=hibernate.reveng.xml
> - generator.hibernate.tables=(table yang mau hibernate example : customer,sales,user,etc)

## Cara Run : 
`mvn entity-repo:run`
