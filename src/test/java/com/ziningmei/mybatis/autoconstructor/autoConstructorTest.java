package com.ziningmei.mybatis.autoconstructor;

import com.ziningmei.mybatis.BaseDataTest;
import com.ziningmei.mybatis.io.Resources;
import com.ziningmei.mybatis.session.SqlSession;
import com.ziningmei.mybatis.session.SqlSessionFactory;
import com.ziningmei.mybatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;

import static org.junit.Assert.assertNotNull;

public class autoConstructorTest {

    private static SqlSessionFactory sqlSessionFactory;


    @BeforeClass
    public static void setUp() throws Exception {
        // create a SqlSessionFactory
        // 创建SqlSessionFactory，核心
        try (Reader reader = Resources.getResourceAsReader("com/ziningmei/mybatis/autoconstructor/mybatis-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }

        // populate in-memory database
        // 使用内存数据库进行测试，执行脚本，导入数据库
        BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
                "com/ziningmei/mybatis/autoconstructor/CreateDB.sql");
    }


    @Test
    public void fullyPopulatedSubject() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
            final PrimitiveSubject subject = mapper.getSubject(1);
            assertNotNull(subject);
        }
    }



}
