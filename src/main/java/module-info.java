/**
 * @author Grégory Van den Borre
 */
module be.yildizgames.common.libloader {
    requires slf4j.api;
    requires be.yildizgames.common.logging;
    requires be.yildizgames.common.compression;
    requires be.yildizgames.common.os;
    requires be.yildizgames.common.exception;

    exports be.yildizgames.common.libloader;
}
