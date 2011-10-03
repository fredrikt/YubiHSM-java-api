/*
 * Copyright (c) 2011 United ID. All rights reserved.
 * Copyright (c) 2011 Yubico AB. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Stefan Wold <stefan.wold@unitedid.org>
 * @author Fredrik Thulin <fredrik@yubico.com>
 */

package org.unitedid.yhsm.internal;

import static org.unitedid.yhsm.internal.Defines.*;
import static org.unitedid.yhsm.utility.Utils.*;

import org.unitedid.yhsm.utility.Utils;

public class UnlockCmd {

    /** Constructor */
    private UnlockCmd() {}
    
    /**
     * Decrypt the YubiHSM key storage using the Master key.
     *
     * @param device the YubiHSM device
     * @param password the password in hex format (see output of automatic password generation during HSM configuration)
     * @return true if unlock was successful
     * @throws YubiHSMErrorException error exceptions
     * @throws YubiHSMInputException argument exceptions
     * @throws YubiHSMCommandFailedException command failed exception
     */
    public static boolean decrypt(DeviceHandler device, String password) throws YubiHSMErrorException, YubiHSMInputException, YubiHSMCommandFailedException {
        byte[] pw = hexToByteArray(password);
        byte[] passwordBA = validateByteArray("password", pw, YSM_MAX_KEY_SIZE, 0, YSM_MAX_KEY_SIZE);
        return parseResult(CommandHandler.execute(device, YSM_KEY_STORE_DECRYPT, passwordBA, true), YSM_KEY_STORE_DECRYPT);
    }

    /**
     * Have the YubiHSM unlock the HSM operations (those involving the keystore) with a YubiKey OTP.
     *
     * @param device the YubiHSM device
     * @param publicId the YubiKey public id
     * @param otp the YubiKey OTP (in hex)
     * @return true if unlock was successful
     * @throws YubiHSMErrorException error exceptions
     * @throws YubiHSMInputException argument exceptions
     * @throws YubiHSMCommandFailedException command failed exception
     */
    public static boolean unlockOtp(DeviceHandler device, String publicId, String otp) throws YubiHSMErrorException, YubiHSMInputException, YubiHSMCommandFailedException {
        byte[] idBA = validateByteArray("publicId", hexToByteArray(publicId), 0, YSM_AEAD_NONCE_SIZE, YSM_AEAD_NONCE_SIZE);
        byte[] otpBA = validateByteArray("otp", hexToByteArray(otp), 0, YSM_OTP_SIZE, YSM_OTP_SIZE);
        byte[] payload = Utils.concatAllArrays(idBA, otpBA);
        return parseResult(CommandHandler.execute(device, YSM_HSM_UNLOCK, payload, true), YSM_HSM_UNLOCK);
    }

    /**
     *  Parse the response from the YubiHSM for a previous command.
     *
     * @param result the result from the last command
     * @param command the YubiHSM command executed
     * @return boolean indicating success
     * @throws YubiHSMCommandFailedException command failed exception
     */
    private static boolean parseResult(byte[] result, byte command) throws YubiHSMCommandFailedException {
        if (result[0] == YSM_STATUS_OK) {
            return true;
        } else if (command == YSM_KEY_STORE_DECRYPT && result[0] == YSM_MISMATCH) {
            return false;
        } else if (command == YSM_HSM_UNLOCK && result[0] == YSM_OTP_INVALID) {
        	return false;
        } else {
            throw new YubiHSMCommandFailedException("Command " + getCommandString(command) + " failed: " + getCommandStatus(result[0]));
        }
    }
}
