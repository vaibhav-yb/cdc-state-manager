# CDC State Manager
A stateless tool to manage the streams and cdc_state table in YugabyteDB.

## Usage
This tool can be used to manipulate and retrieve information from the cdc_state table in YugabyteDB. The user just needs to provide the required options in order to get the tool in action.

### Parameters
These parameters are the required parameters and should be considered as a prerequisite:
| Option | Description |
| :--- | :--- |
| `-master_addresses` | List of comma separated values of master IPs in the format `host1:port1,host2:port2` |
| `-stream_id` | The DB stream ID to operate on |
| `-ssl_root_cert` | Path to SSL certificate file is SSL is enabled |
| `-client_cert_file` | Path to client certificate file if required |
| `-client_key_file` | Path to client key file if required |

**Note:** Because of the dependency of the underlying APIs, you will need to provide a `table_id` along with `tablet_id` in the `-set_checkpoint`, `-clean_tablet` and `-clean_all_tablets` operations.

<!-- ### Operations
| Option | Arguments (if any) | Description |
| :--- | :---: | :--- |
| `-get_db_stream_info` | | Print the information related to the provided stream ID |
| `-get_checkpoints` | | Get the checkpoints of all the tablets associated with the provided stream ID |
| `-get_checkpoint` | | Get the checkpoint of the specified tablet (if it's a part of the provided stream ID |
| `-set_checkpoint` | `<term>.<index>` | Set the checkpoint for a tablet |
| `-bootstrap` | | Bootstrap the tablet while setting a checkpoint |
| `-clean_tablet` | | Set the checkpoint of the provided tablet to a max value |
| `-clean_all_tablets` | | Set the checkpoint of all the tablets associated with a given stream ID to a max value | -->

### Sample commands for operations

#### -get_db_stream_info
Print the information related to the provided stream ID

```sh
java -jar target/yb-cdc-state-manager.jar
  -master_addresses <master-addresses>
  -stream_id <stream-id>
  [-ssl_root_cert <path-to-root-cert>]
  [-client_cert_file <path-to-client-cert-file>]
  [-client_key_file <path-to-client-key-file>]
  -get_db_stream_info
```

#### -get_checkpoints
Get the checkpoints of all the tablets associated with the provided stream ID

```sh
java -jar target/yb-cdc-state-manager.jar
  -master_addresses <master-addresses>
  -stream_id <stream-id>
  [-ssl_root_cert <path-to-root-cert>]
  [-client_cert_file <path-to-client-cert-file>]
  [-client_key_file <path-to-client-key-file>]
  -get_checkpoints
```
#### -get_checkpoint
Get the checkpoint of the specified tablet (if it's a part of the provided stream ID

```sh
java -jar target/yb-cdc-state-manager.jar
  -master_addresses <master-addresses>
  -stream_id <stream-id>
  [-ssl_root_cert <path-to-root-cert>]
  [-client_cert_file <path-to-client-cert-file>]
  [-client_key_file <path-to-client-key-file>]
  -get_checkpoint -tablet_id <tablet-uuid>
```

#### -set_checkpoint
Set the checkpoint for the given tablet. Specify the `-bootstrap` flag along with it if you want to bootstrap the tablet as well.

```sh
java -jar target/yb-cdc-state-manager.jar
  -master_addresses <master-addresses>
  -stream_id <stream-id>
  [-ssl_root_cert <path-to-root-cert>]
  [-client_cert_file <path-to-client-cert-file>]
  [-client_key_file <path-to-client-key-file>]
  -table_id <table-uuid>
  -tablet_id <tablet-uuid>
  -set_checkpoint <term>.<index>
  [-bootstrap]
```

#### -clean_tablet
Set the checkpoint of the provided tablet to a max value.

```sh
java -jar target/yb-cdc-state-manager.jar
  -master_addresses <master-addresses>
  -stream_id <stream-id>
  [-ssl_root_cert <path-to-root-cert>]
  [-client_cert_file <path-to-client-cert-file>]
  [-client_key_file <path-to-client-key-file>]
  -table_id <table-uuid>
  -clean_tablet -tablet_id <tablet-uuid>
```

#### -clean_all_tablets
Set the checkpoint of all the tablets associated with a given stream ID to a max value


```sh
java -jar target/yb-cdc-state-manager.jar
  -master_addresses <master-addresses>
  -stream_id <stream-id>
  [-ssl_root_cert <path-to-root-cert>]
  [-client_cert_file <path-to-client-cert-file>]
  [-client_key_file <path-to-client-key-file>]
  -clean_all_tablets
```
