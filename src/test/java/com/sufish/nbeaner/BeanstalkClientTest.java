package com.sufish.nbeaner;

import com.sufish.nbeaner.handlers.callback.FailedResponseException;
import com.sufish.nbeaner.handlers.callback.PutRequestCallback;
import com.sufish.nbeaner.pool.BeanstalkClient;
import com.sufish.nbeaner.pool.BeanstalkConnection;
import com.sufish.nbeaner.protocol.Beanstalk;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class BeanstalkClientTest {

    private BeanstalkClient client = new BeanstalkClient(11300, "localhost");
    private List<Integer> addJobs = new LinkedList<>();
    private BeanstalkConnection connection;

    private void markNewJob(int jobId) {
        addJobs.add(jobId);
    }

    @Before
    public void setUp() throws Exception {
        connection = client.getConnection();
    }

    @After
    public void tearDown() {
        for (Integer jobId : addJobs) {
            try {
                connection.delete(jobId);
            } catch (Exception e) {

            }
        }
        connection.release();
    }

    @Test
    public void testPut() throws Exception {
        int jobId = connection.put(new Job("fuck".getBytes(), 65535, 0, 120));
        markNewJob(jobId);
        TubeStatus tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(1);
        connection.delete(jobId);
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(0);
    }

    @Test
    public void testPutAsync() throws Exception {
        final int[] newJobId = {-1};
        connection.put(new Job("fuck".getBytes(), 65535, 0, 120), new PutRequestCallback() {

            @Override
            public void onPutSuccess(int jobId) {
                newJobId[0] = jobId;
                markNewJob(jobId);
            }

            @Override
            public void onException(Exception cause) {
            }
        });
        Thread.sleep(1000);
        assertThat(newJobId[0]).isNotEqualTo(-1);
        TubeStatus tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(1);
        connection.delete(newJobId[0]);
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(0);
    }

    @Test
    public void testReserve() throws Exception {
        int jobId = connection.put(new Job("test".getBytes(), 65535, 0, 120));
        markNewJob(jobId);
        TubeStatus tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(1);
        Job job = connection.reserve();
        assertThat(job.getId()).isEqualTo(jobId);
        assertThat(job.getJobData()).isEqualTo("test".getBytes());
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(0);
        assertThat(tubeStatus.currentReservedJobs()).isEqualTo(1);
        connection.delete(jobId);
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(0);
        assertThat(tubeStatus.currentReservedJobs()).isEqualTo(0);
    }

    @Test
    public void testRelease() throws Exception {
        int jobId = connection.put(new Job("test".getBytes(), 65535, 0, 120));
        markNewJob(jobId);
        TubeStatus tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(1);
        Job job = connection.reserve();
        assertThat(job.getId()).isEqualTo(jobId);
        assertThat(job.getJobData()).isEqualTo("test".getBytes());
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(0);
        assertThat(tubeStatus.currentReservedJobs()).isEqualTo(1);

        connection.releaseJob(jobId, 62225, 0);
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(1);
        assertThat(tubeStatus.currentReservedJobs()).isEqualTo(0);

        connection.delete(jobId);
        tubeStatus = connection.statsTube("default");
        assertThat(tubeStatus.currentReadyJobs()).isEqualTo(0);
        assertThat(tubeStatus.currentReservedJobs()).isEqualTo(0);
    }

    @Test
    public void testDeleteException() throws Exception {
        try {
            BeanstalkConnection connection = client.getConnection();
            connection.delete(10000);
        } catch (FailedResponseException e) {
            if (!e.getResponseText().equals(Beanstalk.NOT_FOUND)) {
                fail("should get " + Beanstalk.NOT_FOUND);
            }
        }
    }
}
