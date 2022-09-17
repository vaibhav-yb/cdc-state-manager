package org.yb.cdc;

/**
 * Hello world!
 *
 */
public class App 
{
    private final CmdLineOpts configuration;
    private final Functions functions;
    
    public App(CmdLineOpts configuration) {
        this.configuration = configuration;
        this.functions = new Functions(configuration);
    }

    public void run() {
        try {
            functions.execute();
        } catch (Exception e) {
            System.out.println("Exception thrown while executing");
            e.printStackTrace();
            System.exit(-1);
        }
    }
    public static void main( String[] args )
    {
        CmdLineOpts configuration = new CmdLineOpts(args);
        App app = new App(configuration);
        app.run();
        System.exit(0);
    }
}
