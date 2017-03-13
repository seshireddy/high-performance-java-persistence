package com.vladmihalcea.book.hpjp.hibernate.fetching;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import com.vladmihalcea.book.hpjp.util.AbstractMySQLIntegrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.LongStream;

/**
 * @author Vlad Mihalcea
 */
@RunWith(Parameterized.class)
public class MySQLScrollableResultsStreamingTest extends AbstractMySQLIntegrationTest {

    private MetricRegistry metricRegistry = new MetricRegistry();

    private Timer timer = metricRegistry.timer(getClass().getSimpleName());

    private Slf4jReporter logReporter = Slf4jReporter
            .forRegistry(metricRegistry)
            .outputTo(LOGGER)
            .build();

    private final int resultSetSize;

    public MySQLScrollableResultsStreamingTest(Integer resultSetSize) {
        this.resultSetSize = resultSetSize;
    }

    @Override
    protected Class<?>[] entities() {
        return new Class[]{
            Post.class
        };
    }

    @Parameterized.Parameters
    public static Collection<Integer[]> parameters() {
        List<Integer[]> providers = new ArrayList<>();
        providers.add(new Integer[]{1});
        providers.add(new Integer[]{2});
        providers.add(new Integer[]{5});
        providers.add(new Integer[]{10});
        providers.add(new Integer[]{25});
        providers.add(new Integer[]{50});
        providers.add(new Integer[]{75});
        providers.add(new Integer[]{100});
        providers.add(new Integer[]{250});
        providers.add(new Integer[]{500});
        providers.add(new Integer[]{750});
        providers.add(new Integer[]{1000});
        providers.add(new Integer[]{1500});
        providers.add(new Integer[]{2000});
        providers.add(new Integer[]{2500});
        providers.add(new Integer[]{5000});
        return providers;
    }

    @Override
    public void init() {
        super.init();
        doInJPA(entityManager -> {
            LongStream.range(0, 5000).forEach(i -> {
                Post post = new Post(i);
                post.setTitle(String.format("Post nr. %d", i));
                entityManager.persist(post);
                if(i % 50 == 0 && i > 0) {
                    entityManager.flush();
                }
            });
        });
    }

    @Override
    protected Properties properties() {
        Properties properties = super.properties();
        properties.put("hibernate.jdbc.batch_size", "50");
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        return properties;
    }

    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post {

        @Id
        private Long id;

        private String title;

        public Post() {
        }

        public Post(Long id) {
            this.id = id;
        }

        public Post(String title) {
            this.title = title;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
