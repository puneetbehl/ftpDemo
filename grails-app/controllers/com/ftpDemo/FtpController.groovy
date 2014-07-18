package com.ftpDemo

/**
 * Created by behl on 1/8/14.
 */
class FtpController {

    def ftpService

    def upload() {
        File file = File.createTempFile("temp", "txt")
        file.text = "Sample File\n"
        FtpCredential ftpCredential = new FtpCredential(server: 'frs.sourceforge.net', username: 'behl', password: 'test@12345', port: 22, remoteBaseDir: '/home/frs/project/sampleftpdemo')
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file))
        ftpService.save(inputStream, "sampleFile.txt", ftpCredential)

    }
}
