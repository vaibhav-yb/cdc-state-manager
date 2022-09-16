package org.yb.cdc;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;

public class CmdLineOpts {
  private static final Logger LOGGER = LoggerFactory.getLogger(CmdLineOpts.class);
  
  private static final String DEFAULT_MASTER_ADDRESSES = "127.0.0.1:7100";
  
  private String masterAddresses;
  private String streamId;
  private String tabletId;
  private String sslCertFile;
  private String clientKeyFile;
  private String clientCertFile;
  private int maxTablets = 100;
  private long term;
  private long index;

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

  public static CmdLineOpts createFromArgs(String[] args) {
    Options options = new Options();

    options.addOption("master-addresses", true, "List of YB master IPs to connect to");

    options.addOption("stream-id", true, "The stream ID on which we need to perform operations");

    options.addOption(
        "get-checkpoint", false, "Option to specify that we need to print the checkpoints");
    
    options.addOption("tablet-id", true, "The tablet ID on which we need to perform operations");

    options.addOption("clean-all", false, 
                      "Set the checkpoints of all the tablets for a given stream ID to OpId::Max");

    options.addOption("set-checkpoint", true, "Checkpoint to set for a given tablet");
    options.addOption("bootstrap", false, "Whether to bootstrap while setting checkpoint");  
    
    options.addOption(
        "get-db-stream-info", false, "Return the DB stream information of a given stream");

    options.addOption("ssl-cert-file", true, "Path to root certificate");
    options.addOption("client-cert-file", true, "Path to client certificate");
    options.addOption("client-key-file", true, "Path to client key file");

    options.addOption("max-tablets", true, "Number of tablets the yb-client should fetch");

    // Do the parsing
    CommandLineParser parser = new DefaultParser();
    CommandLine commandLine = null;
    
    try {
        commandLine = parser.parse(options, args);
    } catch (Exception e) {
      LOGGER.error("Error while parsing arguments from command line: {}", e);
      System.exit(-1);
    }

    CmdLineOpts configuration = new CmdLineOpts();
    configuration.initialize(commandLine);
    
    return configuration;
  }

  private void initialize(CommandLine commandLine) {
    if (commandLine.hasOption("master-addresses")) {
      this.setMasterAddresses(commandLine.getOptionValue("master-addresses"));
    } else {
      LOGGER.error("Please specify master addresses using the --master-addresses option");
      System.exit(404);
    }

    if (commandLine.hasOption("stream-id")) {
      this.setStreamId(commandLine.getOptionValue("stream-id"));
    } else {
      LOGGER.error("Please specify stream ID using the --stream-id option");
      System.exit(404);
    }

    
  }
}
