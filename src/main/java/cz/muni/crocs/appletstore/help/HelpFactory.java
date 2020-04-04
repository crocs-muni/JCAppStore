package cz.muni.crocs.appletstore.help;

public class HelpFactory {

    public static Help getMasterKeyHelp() {
        return new HelpBuilder(35, 20, 600)
                .addTitle("mk_title")
                .addText("mk_intro")
                .addSubTitle("mk_title_ini")
                .addText("mk_ini");
    }

    public static Help getCmdHelp() {
        return new HelpBuilder(35, 20, 600)
                .addTitle("cmd_title")
                .addText("cmd_introduction")
                .addSubTitle("cmd_browse_title")
                .addText("cmd_browse")
                .addSubTitle("cmd_launch_linux_title")
                .addText("cmd_launch_linux")
                .addSubTitle("cmd_launch_win_title")
                .addText("cmd_launch_win")
                .addSubTitle("cmd_lanuch_Java_title")
                .addText("cmd_lanuch_Java");
    }

    public static Help getAppletUsageHelp() {
        return new HelpBuilder(35, 20, 600)
                .addTitle("au_title")
                .addText("au_introduction")
                .addSubTitle("au_host_title")
                .addText("au_host")
                .addSubTitle("au_no_host_title")
                .addText("au_no_host");
    }

    public static Help getMainAppletHelp() {
        return new HelpBuilder(35, 20, 600)
                .addTitle("def_title")
                .addText("def_introduction")
                .addSubTitle("def_why_title")
                .addText("def_why")
                .addSubTitle("def_why_main_title")
                .addText("def_why_main");
    }

    public static Help getInstallFailuresFAQ() {
        return new HelpBuilder(35, 20, 600)
                .addTitle("ifaq_title")
                .addSubTitle("ifaq_cardstate_title")
                .addText("ifaq_cardstate")
                .addSubTitle("ifaq_low_memory_title")
                .addText("ifaq_low_memory")
                .addSubTitle("ifaq_wrong_sdk_title")
                .addText("ifaq_wrong_sdk")
                .addSubTitle("ifaq_ip_title")
                .addText("ifaq_ip")
                .addSubTitle("ifaq_tech_title")
                .addText("ifaq_tech")
                .addSubTitle("ifaq_general_title")
                .addText("ifaq_general");
    }
}
