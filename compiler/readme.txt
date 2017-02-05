编译过程：

1、把mif文件转成shp
   安装osgeo4w (win版本)
   启动 osgeo4w shell
   使用ogr2ogr 命令把mif文件转shp:  ogr2ogr Nbeijing.shp Nbeijing.mif
   
   
2、shp文件转csv

    使用com.cennavi.compiler.map.ShpfileCompiler 得到下面三种数据：
    N.csv ->Node数据
    R.csv ->Link数据
    C.csv ->小网格数据
    
    
3、路径预处理
   1、N.csv里面数据有重复的，需要排重后才能导入Neo4j，R表没有重复的。
      linux 下排重:cat N.csv | sort | uniq > uN.csv
   2、使用com.cennavi.compiler.neo4j.DirectImport2Neo4j把N表和R表数据导入neo4j
   3、使用com.cennavi.compiler.neo4j.RouteTraverser生成预处理路径的route.csv文件
                注意:把neo4j数据放入缓存加速，使用一个线程，多线程现在还有问题。这个过程比较慢
                北京的数据跑了3个小时左右


4、导入lucene
   1、使用com.cennavi.compiler.lucence.BasicNetworkIndexer把N表和R表数据导入lucene
   2、使用com.cennavi.compiler.lucence.RouteTreeIndexer把route.csv导入lucene

   