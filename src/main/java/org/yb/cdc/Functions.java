package org.yb.cdc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yb.Opid;
import org.yb.Opid.OpIdPB;
import org.yb.client.AsyncYBClient;
import org.yb.client.GetCheckpointResponse;
import org.yb.client.GetDBStreamInfoResponse;
import org.yb.client.ListTablesResponse;
import org.yb.client.SetCheckpointResponse;
import org.yb.client.YBClient;
import org.yb.client.YBTable;
import org.yb.master.MasterDdlOuterClass.ListTablesResponsePB.TableInfo;

public class Functions {
  private static final Logger LOGGER = LoggerFactory.getLogger(Functions.class);
  private final YBClient ybClient;
  private final CmdLineOpts config;

  private Map<String, YBTable> tableIdToTable = new HashMap<>();
  private Map<String, String> tableIdToSchemaName = new HashMap<>();

  public Functions(CmdLineOpts options) {
    this.config = options;

    AsyncYBClient asyncClient = 
        new AsyncYBClient.AsyncYBClientBuilder(options.getMasterAddresses())
            .defaultAdminOperationTimeoutMs(60000)
            .defaultOperationTimeoutMs(60000)
            .defaultSocketReadTimeoutMs(60000)
            .sslCertFile(options.getSslCertFile())
            .sslClientCertFiles(options.getClientCertFile(), options.getClientKeyFile())
            .numTablets(options.getMaxTablets())
            .build();

    this.ybClient = new YBClient(asyncClient);

    try {
      // Cache all the tables
      ListTablesResponse listResponse = this.ybClient.getTablesList();
      for (TableInfo tableInfo : listResponse.getTableInfoList()) {
        String tableId = tableInfo.getId().toStringUtf8();
        tableIdToTable.put(tableId, this.ybClient.openTableByUUID(tableId));
        tableIdToSchemaName.put(tableId, tableInfo.getPgschemaName());
      }
    } catch (Exception e) {
      LOGGER.error("Error while listing tables, {}", e);
    }
    
  }

  public CommandLine getCommandLine() {
    return this.config.getCommandLine();
  }

  public CmdLineOpts getConfig() {
    return this.config;
  }

  public void execute() throws Exception {
    if (getCommandLine().hasOption("get_db_stream_info")) {
      getDbStreamInfo(getConfig().getStreamId());
      return;
    }

    if (getCommandLine().hasOption("get_checkpoint")) {
      getCheckpoint(getConfig().getStreamId(), getConfig().getTabletId());
      return;
    }

    if (getCommandLine().hasOption("get_checkpoints")) {
      // Get the tablet IDs in the stream
      String streamId = getConfig().getStreamId();
      Set<String> tableIDs = getTableIdsPartOfStream(streamId);

      for (String tableId : tableIDs) {
        Set<String> tabletIDs = this.ybClient.getTabletUUIDs(tableIdToTable.get(tableId));
        for (String tabletId : tabletIDs) {
          getCheckpoint(streamId, tabletId);
        }
      }
      return;
    }

    if (getCommandLine().hasOption("clean_tablet")) {
      String streamId = getConfig().getStreamId();
      String tableId = getConfig().getTableId();
      String tabletId = getConfig().getTabletId();

      cleanTablet(streamId, tableId, tabletId);
      return;
    }

    if (getCommandLine().hasOption("clean_all_tablets")) {
      String streamId = getConfig().getStreamId();
      Set<String> tableIDs = getTableIdsPartOfStream(streamId);
      for (String tableId : tableIDs) {
        Set<String> tabletIDs = this.ybClient.getTabletUUIDs(tableIdToTable.get(tableId));
        for (String tabletId : tabletIDs) {
          cleanTablet(streamId, tableId, tabletId);
        }
      }
      return;
    }

    if (getCommandLine().hasOption("set_checkpoint")) {
      String streamId = getConfig().getStreamId();
      String tableId = getConfig().getTableId();
      String tabletId = getConfig().getTabletId();
      long term = getConfig().getTerm();
      long index = getConfig().getIndex();

      setCheckpoint(streamId, tableId, tabletId, term, index);
      return;
    }
  }

  public Set<String> getTableIdsPartOfStream(String streamId) throws Exception {
    Set<String> res = new HashSet<>();

    GetDBStreamInfoResponse resp = this.ybClient.getDBStreamInfo(streamId);

    for (org.yb.master.MasterReplicationOuterClass.GetCDCDBStreamInfoResponsePB.TableInfo tableInfo 
            : resp.getTableInfoList()) {
      res.add(tableInfo.getTableId().toStringUtf8());
    }

    return res;
  }

  public void getDbStreamInfo(String streamId) throws Exception {
    GetDBStreamInfoResponse resp = this.ybClient.getDBStreamInfo(streamId);

    LOGGER.info("Stream ID: {}, List of tables in it:", streamId);
    for (org.yb.master.MasterReplicationOuterClass.GetCDCDBStreamInfoResponsePB.TableInfo tableInfo 
            : resp.getTableInfoList()) {
      String tableId = tableInfo.getTableId().toStringUtf8();
      YBTable ybTable = tableIdToTable.get(tableId); //this.ybClient.openTableByUUID(tableId);
      LOGGER.info("Table ID: {} ({}.{}.{})", tableId, 
                  ybTable.getKeyspace(), tableIdToSchemaName.get(tableId), ybTable.getName());
    }
  }

  public void getCheckpoint(String streamId, String tabletId) throws Exception {
    GetCheckpointResponse resp = 
        this.ybClient.getCheckpoint(
            tableIdToTable.entrySet().iterator().next().getValue(), streamId, tabletId);

    LOGGER.info("Stream ID: {} Tablet ID: {} Checkpoint: {}.{}", 
                streamId, tabletId, resp.getTerm(), resp.getIndex());
  }

  public void cleanTablet(String streamId, String tableId, String tabletId) throws Exception {
    setCheckpoint(streamId, tableId, tabletId, Long.MAX_VALUE, Long.MAX_VALUE);
  }

  public void setCheckpoint(String streamId, String tableId, 
                            String tabletId, long term, long index) throws Exception {
    this.ybClient.bootstrapTablet(tableIdToTable.get(tableId), streamId, tabletId, 
                                  term, index, false, 
                                  getConfig().shouldBootstrap());

    LOGGER.info("Successfully set checkpoint as {}.{} for tablet {}", term, index, tabletId);
  }
}
