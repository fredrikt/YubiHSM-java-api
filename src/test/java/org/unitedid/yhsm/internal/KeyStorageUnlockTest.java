/*
 * Copyright (c) 2011 United ID. All rights reserved.
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
 */

package org.unitedid.yhsm.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.unitedid.yhsm.SetupCommon;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeyStorageUnlockTest extends SetupCommon {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void failedUnlockHsm() throws YubiHSMCommandFailedException, YubiHSMErrorException, YubiHSMInputException {
        assertFalse(hsm.keyStorageUnlock("1111"));
    }

    @Test
    public void unlockHsm() throws Exception {
        assertTrue(hsm.keyStorageUnlock("2f6af1e667456bb94528e7987344515b"));
    }

    @Test
    public void otpUnlockHsm() throws Exception {
        if (new Integer(hsm.info().get("major")) > 0) {
            /* Incorrect public id */
            thrown.expect(YubiHSMCommandFailedException.class);
            thrown.expectMessage("Command YSM_HSM_UNLOCK failed: YSM_INVALID_PARAMETER");
            assertFalse(hsm.unlockOtp("010000000000", "ffaaffaaffaaffaaffaaffaaffaaffaa"));
            thrown = ExpectedException.none();

            /* Right public id, wrong OTP */
            assertFalse(hsm.unlockOtp("4d4d4d000001", "ffaaffaaffaaffaaffaaffaaffaaffaa"));

            /* Right public id, right OTP (for counter values 1/0) */
            assertTrue(hsm.unlockOtp("4d4d4d000001", "caa821a197c50a29e9fd5bcc35fc4f6d"));

            /* Replay, will lock the HSM again */
            thrown.expect(YubiHSMCommandFailedException.class);
            thrown.expectMessage("Command YSM_HSM_UNLOCK failed: YSM_OTP_REPLAY");
            assertFalse(hsm.unlockOtp("4d4d4d000001", "caa821a197c50a29e9fd5bcc35fc4f6d"));
            thrown = ExpectedException.none();

            /* Right public id, new OTP */
            assertTrue(hsm.unlockOtp("4d4d4d000001", "f8df012a2072e6a4d337a6c8c802a75c"));
        }
    }
}
