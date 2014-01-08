package com.ftpDemo

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

/**
 * Created by behl on 1/8/14.
 */
class FtpService {
    def grailsApplication

    static transactional = true

    def save(InputStream inputStream, String fileName, FtpCredential ftpCredential) {
        connect(ftpCredential) { ChannelSftp sftp ->
            sftp.put inputStream, fileName
        }
    }

    def load(String fileName, FtpCredential ftpCredential) {
        connect(ftpCredential, { ChannelSftp sftp ->
            File outputFile = File.createTempFile(fileName, '')
            outputFile?.newOutputStream() << sftp.get(fileName)
            outputFile
        }, false)
    }

    def delete(String fileName, FtpCredential ftpCredential) throws Throwable {
        connect(ftpCredential) { ChannelSftp sftp ->
            sftp.rm fileName
        }
    }

    def makeDir(String directoryName, FtpCredential ftpCredential) {
        connect(ftpCredential) { ChannelSftp sftp ->
            sftp.mkdir directoryName
        }
    }

    private def connect(FtpCredential ftpCredential, Closure c, boolean disconnectOnFinish = true) {
        Session session = null
        ChannelSftp sftp = null
        try {
            JSch jSch = new JSch()
            session = jSch.getSession ftpCredential?.username, ftpCredential?.server, ftpCredential?.port
            session.setConfig "StrictHostKeyChecking", "no"
            File keyFile = new File("${grailsApplication.config.pathToKeyFile}")
            if (ftpCredential?.password) {
                session.password = ftpCredential?.password
            } else {
                jSch.addIdentity(keyFile?.absolutePath)
            }
            session.connect()
            Channel sFtpChannel = session.openChannel "sftp"
            sFtpChannel.connect()
            sftp = sFtpChannel as ChannelSftp
            sftp.cd ftpCredential?.remoteBaseDir
            c.call sftp
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (disconnectOnFinish) {
                sftp?.exit()
                session?.disconnect()
            }
        }
    }
}
