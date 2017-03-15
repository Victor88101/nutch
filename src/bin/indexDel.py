#!/usr/bin/python
# -*- coding:UTF-8 -*-
import MySQLdb
import sys
import urllib 
import urllib3 

try: 
    http_pool=urllib3.HTTPConnectionPool('solr1.imcle.com','8080')  
    sql='SELECT UrlHost FROM ENT_NetMedia ,ENT_Enterprise WHERE ENT_NetMedia.EnterpriseID = ENT_Enterprise.ID AND ( ENT_NetMedia.Status&32 = 32 OR ENT_Enterprise.Status&6 = 6)'
    conn=MySQLdb.connect(host='database1.imcle.com',user='search',passwd='P@ssw0rd',db='TTROS_GroupMng',port=3306)
#!/usr/bin/python
# -*- coding:UTF-8 -*-
import MySQLdb
import sys
import urllib 
import urllib3 

try: 
    http_pool=urllib3.HTTPConnectionPool('solr1.imcle.com','8080')  
    sql='SELECT UrlHost FROM ENT_NetMedia ,ENT_Enterprise WHERE ENT_NetMedia.EnterpriseID = ENT_Enterprise.ID AND ( ENT_NetMedia.Status&32 = 32 OR ENT_Enterprise.Status&4 = 4)'
    conn=MySQLdb.connect(host='database1.imcle.com',user='search',passwd='P@ssw0rd',db='TTROS_GroupMng',port=3306)
    cur=conn.cursor()
    count=cur.execute(sql)
    print 'there has %s urlhost record' % count

    results=cur.fetchall()
    for r in results:
        print 'Del %s solr index' % r[0]
        url="/solr/myEnterpriseCollection_shard1_replica1/update/?stream.body=<delete><query>host:\""+r[0]+"\"</query></delete>&stream.contentType=text/xml;charset=utf-8"
        r = http_pool.urlopen('GET',url,redirect=False)  
        print r.status,r.headers,len(r.data)  
          
    cur.close()
    conn.close()
    print 'Del commit,total %s index will be deleted!' % count
    url="/solr/myEnterpriseCollection_shard1_replica1/update/?commit=true"
    r = http_pool.urlopen('GET',url,redirect=False)  
    print r.status,r.headers,len(r.data) 

    sys.exit(0)
except MySQLdb.Error,e:
    print "Mysql Error %d: %s" % (e.args[0], e.args[1])
except urllib3.exceptions.HTTPError,e:    
    print "Http Error :",e 