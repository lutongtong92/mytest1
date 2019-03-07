package com.lucene;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;

/**
 * @author lu
 * @version v1.0
 * @date 2019/3/6 10:09
 * @description TODO
 **/



public class IndexMannager {
    @Test
//test注解使用要求  1.必须是public权限 2.必须是void 3.一定是无参
//    1.创建索引
    public void testWriter() throws Exception{
//        2.准备一个存储索引文件的路径  FS:FileSystem
//        new File一定会报IO异常
        Directory directory = FSDirectory.open(new File("D:\\Java\\ideawork\\Project\\index_repo"));
//        3.new IndexWriterConfig
/*        Version matchVersion,  指定版本Version.LATEST最新
          Analyzer analyzer  分词器  指定使用哪种分词器
          */
//        4.创建标准分词器将对象传给IndexWriterConfig
        /*Analyzer ctrl+alt+B 查看它的子类*/
        //Analyzer analyzer = new StandardAnalyzer();
        IKAnalyzer analyzer = new IKAnalyzer();

        IndexWriterConfig conf = new IndexWriterConfig(Version.LATEST,analyzer);
//        1.创建Indexwriter对象
//        需要参数
        /*Directory directory, 准备一个存储索引文件的路径
         IndexWriterConfig conf
         */
        IndexWriter indexWriter = new IndexWriter(directory,conf);
//      删除所有索引
        indexWriter.deleteAll();
//      9.遍历文档  将所有文件所有内容放到索引库中
        File filePaths = new File("C:\\java-web\\就业班上课资料\\项目一\\Lucene\\资料\\上课用的查询资料searchsource");
        File[] files = filePaths.listFiles();
        for (File file : files) {
//      6.创建Document对象
            Document document=new Document();
//      8.创建IndexableField接口的实现类TextField作为document的属性
//        (String name, String value, Field.Store store)
//      7.document.add()添加属性IndexableField接口的实现类TextField
//            P1:域的名称   P2：值    P3：是否存储原内容  值
//      10.遍历filename
            String fileName = file.getName();
            document.add(new TextField("filename", fileName, Field.Store.YES));
//      11.遍历filecontent
            String fileContent = FileUtils.readFileToString(file, "utf-8");
            document.add(new TextField("filecontent", fileContent, Field.Store.YES));
//      12.遍历filepath
            String filePath = file.getPath();
            //路径不要分词 选择StringField
            document.add(new StringField("filepath", filePath, Field.Store.YES));
//      13.遍历filesize
            long fileSize = FileUtils.sizeOf(file);
            /*注意此处不用TextField 与参数类型不匹配 */
            document.add(new LongField("filesize", fileSize, Field.Store.YES));

//      5.indexWriter用来写入对象
//      添加
            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }


    @Test
//    1.从索引中查询
    public void indexReader() throws Exception{
//      2.创建索引库
        Directory directory = FSDirectory.open(new File("D:\\Java\\ideawork\\Project\\index_repo"));

//      1.读取哪个索引的文件 DirectoryReader.open()
//        创建读取文档的对象
        IndexReader indexReader = DirectoryReader.open(directory);
//      2.创建查询的对象，创建时需要把indexReader 构建进去
//        根据indexReader查询,查询的是directory位置
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
//      3.按照term查询  域名：值   需求：查询标题带apache的
//      4.根据Query对象查询
//        Query是一个抽象类，抽象类中有许多实现类 创建TermQuery实现类,参数中传入Term，new Term(域名：值)
        Query query = new TermQuery(new Term("filename","全文检索"));
//      5. 根据query查询语法查询，查询出最顶部10条数据(文章)
        TopDocs topDocs = indexSearcher.search(query, 10);
//      6.查询文章的内容封装再数组中
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
//      7.遍历
        for (ScoreDoc scoreDoc : scoreDocs) {
//          倒排索引结构 一个分词对应文档的ID(可能有多个)
//           目标：根据ID获取文档的内容
            int docID = scoreDoc.doc;
//           根据文档查询 需要传入参数 文档的ID
            Document document = indexSearcher.doc(docID);
//          通过document对象根据域的名字获取文档中的内容,并打印出来
            System.out.println("标题"+document.get("filename"));
            System.out.println("内容"+document.get("filecontent"));
            System.out.println("路径"+document.get("filepath"));
            System.out.println("大小"+document.get("filesize"));
            System.out.println("----------------------------------");

        }
        indexReader.close();
    }

    @Test
//标准分词器
    public void testAnalyzer() throws Exception{
//      StandardAnalyzer的父类的父类是Analyzer
//      1.创建标准分词器
        //Analyzer analyzer = new StandardAnalyzer();

//       创建CJKAnalyzer中文分词器
        //CJKAnalyzer analyzer = new CJKAnalyzer();

//        创建SmartChineseAnalyzer中文分词器
        //SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();

//        创建IKAnalyzer中文分词器
        IKAnalyzer analyzer = new IKAnalyzer();
//      2.通过analyzer调用tokenStream(域名，文本内容)方法进行分词
        TokenStream tokenStream = analyzer.tokenStream("test", "创建是传智播客一个白面郎君基于Reader的Tokenizer分词器的嘛");
//      3.设置引用，为了获取每个分词的结果,字符串分词属性
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
//      4.指针位置归0  给指针指定第一个位置就是0索引位
        tokenStream.reset();
//      5. 判断 当指针指下去的位置没有内容就结束
        while(tokenStream.incrementToken()){
//       6.打印分词后的结果
            System.out.println(charTermAttribute);
        }

    }



}
