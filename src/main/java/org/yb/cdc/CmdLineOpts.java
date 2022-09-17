package org.yb.cdc;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdLineOpts {
  private static final Logger LOGGER = LoggerFactory.getLogger(CmdLineOpts.class);
  
  private static final String DEFAULT_MASTER_ADDRESSES = "127.0.0.1:7100";

  private CommandLine commandLine;
  
  private String masterAddresses;
  private String tableName;
  private String streamId;
  private String tabletId;
  private String sslCertFile;
  private String clientKeyFile;
  private String clientCertFile;
  private int maxTablets = 100;
  private long term;
  private long index;

  public String getTableName() {
    return this.tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public long getTerm() {
    return this.term;
  }

  public void setTerm(long term) {
    this.term = term;
  }

  public long getIndex() {
    return this.index;
  }

  public void setIndex(long index) {
    this.index = index;
  }

  private boolean bootstrap = false;

  public boolean shouldBootstrap() {
    return this.bootstrap;
  }

  public void setBootstrap(boolean bootstrap) {
    this.bootstrap = bootstrap;
  }

  public String getMasterAddresses() {
    return this.masterAddresses;
  }

  public void setMasterAddresses(String masterAddresses) {
    this.masterAddresses = masterAddresses;
  }

  public String getStreamId() {
    return this.streamId;
  }

  public void setStreamId(String streamId) {
    this.streamId = streamId;
  }

  public String getTabletId() {
    return this.tabletId;
  }

  public void setTabletId(String tabletId) {
    this.tabletId = tabletId;
  }

  public String getSslCertFile() {
    return this.sslCertFile;
  }

  public void setSslCertFile(String sslCertFile) {
    this.sslCertFile = sslCertFile;
  }

  public String getClientKeyFile() {
    return this.clientKeyFile;
  }

  public void setClientKeyFile(String clientKeyFile) {
    this.clientKeyFile = clientKeyFile;
  }

  public String getClientCertFile() {
    return this.clientCertFile;
  }

  public void setClientCertFile(String clientCertFile) {
    this.clientCertFile = clientCertFile;
  }

  public int getMaxTablets() {
    return this.maxTablets;
  }

  public void setMaxTablets(int maxTablets) {
    this.maxTablets = maxTablets;
  }

  public CommandLine getCommandLine() {
    return this.commandLine;
  }

  public CmdLineOpts(String[] args) {
    Options options = new Options();

    options.addOption("master_addresses", true, "List of YB master IPs to connect to");

    options.addOption("stream_id", true, "The stream ID on which we need to perform operations");

    options.addOption("table_name", true, "Name of the table");

    options.addOption(
        "get_checkpoint", false, "Option to specify that we need to print the checkpoints");
    options.addOption("get_checkpoints", false, "Specify that we need checkpoints of all the tablets");
    
    options.addOption("tablet_id", true, "The tablet ID on which we need to perform operations");

    options.addOption("clean_all", false, 
                      "Set the checkpoints of all the tablets for a given stream ID to OpId::Max");

    options.addOption("set_checkpoint", true, "Checkpoint to set for a given tablet");
    options.addOption("bootstrap", false, "Whether to bootstrap while setting checkpoint");  
    
    options.addOption(
        "get_db_stream_info", false, "Return the DB stream information of a given stream");

    options.addOption("ssl_cert_file", true, "Path to root certificate");
    options.addOption("client_cert_file", true, "Path to client certificate");
    options.addOption("client_key_file", true, "Path to client key file");

    options.addOption("max_tablets", true, "Number of tablets the yb-client should fetch");

    // Do the parsing
    CommandLineParser parser = new DefaultParser();
    this.commandLine = null;
    try {
        this.commandLine = parser.parse(options, args);
    } catch (Exception e) {
      LOGGER.error("Error while parsing arguments from command line: {}", e);
      System.exit(-1);
    }

    initialize(this.commandLine);
    
    // return configuration;
  }

  private void initialize(CommandLine commandLine) {
    if (commandLine.hasOption("master_addresses")) {
      this.setMasterAddresses(commandLine.getOptionValue("master_addresses"));
    } else {
      LOGGER.error("Please specify master addresses using the --master_addresses option");
      System.exit(404);
    }

    if (commandLine.hasOption("stream_id")) {
      this.setStreamId(commandLine.getOptionValue("stream_id"));
    } else {
      LOGGER.error("Please specify stream ID using the --stream_id option");
      System.exit(404);
    }

    if (commandLine.hasOption("get_checkpoint")) {
      if (!commandLine.hasOption("tablet_id")) {
        LOGGER.error("No -tablet_id specified with get_checkpoint, if you want to get the checkpoints for all tablets, use -get_checkpoints");
      }
    }

    if (commandLine.hasOption("get_checkpoint") && commandLine.hasOption("get_checkpoints")) {
      LOGGER.error("Cannot specify both options -get_checkpoint and -get_checkpoints together");
      System.exit(-1);
    }
  }
}
