namespace mgmt_net_workshop
{
    public static class MgmtFactory
    {
        /* the ecs management user name - namespace admin or system admin. */
        public static string MGMT_USER_NAME = "";
        /* the password for the management user */
        public static string MGMT_USER_PASSWORD = "";
        /* the end point of the ECS management api REST interface */
        public static string MGMT_ENDPOINT = "https://IP_OF_ECS:4443";

        /* the namespace administrator - to be created and used in subsequent examples */
        public static string NAMESPACE_ADMIN_NAME = "workshop-admin";
        /* the namespace administrator password */
        public static string NAMESPACE_ADMIN_PASSWORD = "Password1";
        /* the namespace - to be created and used in subsequent examples */
        public static string NAMESPACE_NAME = "workshop";
        /* the object user - to be created and used in subsequent examples */
        public static string OBJECT_USER = "workshop-object-user";
        /* the replication group within to create namespace */
        public static string REPLICATION_GROUP = "";
        /* the bucket name to be created in new namespace */
        public static string BUCKET_NAME = "workshop-bucket";

    }
}
