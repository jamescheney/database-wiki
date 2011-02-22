/* 
    BEGIN LICENSE BLOCK
    Copyright 2010-2011, Heiko Mueller, Sam Lindley, James Cheney and
    University of Edinburgh

    This file is part of Database Wiki.

    Database Wiki is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Database Wiki is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Database Wiki.  If not, see <http://www.gnu.org/licenses/>.
    END LICENSE BLOCK
*/
package org.dbwiki.web.security;

class Base64 {
	/*
	 * Private Variables
	 */
	
	private final byte altBase64ToInt[] = { -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1,
			2, 3, 4, 5, 6, 7, 8, -1, 62, 9, 10, 11, -1, 52, 53, 54, 55,
			56, 57, 58, 59, 60, 61, 12, 13, 14, -1, 15, 63, 16, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, 17, -1, 18, 19, 21, 20,
			26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
			41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 22, 23, 24, 25 };

	private final byte base64ToInt[] = { -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
			55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0,
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
			18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26,
			27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41,
			42, 43, 44, 45, 46, 47, 48, 49, 50, 51 };

	private final char intToAltBase64[] = { '!', '"', '#', '$',
			'%', '&', '\'', '(', ')', ',', '-', '.', ':', ';', '<',
			'>', '@', '[', ']', '^', '`', '_', '{', '|', '}', '~', 'a',
			'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
			'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
			'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '?' };

	private final char intToBase64[] = { 'A', 'B', 'C', 'D',
			'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
			'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
			'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };
	

	/*
	 * Public Methods
	 */

	// Dead code...
	
	public String byteArrayToBase64(byte[] a, boolean alternate) {
		int aLen = a.length;
		int numFullGroups = aLen / 3;
		int numBytesInPartialGroup = aLen - 3 * numFullGroups;
		int resultLen = 4 * ((aLen + 2) / 3);
		StringBuffer result = new StringBuffer(resultLen);
		char[] intToAlpha = (alternate ? intToAltBase64 : intToBase64);

		int inCursor = 0;
		for (int i = 0; i < numFullGroups; i++) {
			int byte0 = a[inCursor++] & 0xff;
			int byte1 = a[inCursor++] & 0xff;
			int byte2 = a[inCursor++] & 0xff;
			result.append(intToAlpha[byte0 >> 2]);
			result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
			result.append(intToAlpha[(byte1 << 2) & 0x3f | (byte2 >> 6)]);
			result.append(intToAlpha[byte2 & 0x3f]);
		}

		if (numBytesInPartialGroup != 0) {
			int byte0 = a[inCursor++] & 0xff;
			result.append(intToAlpha[byte0 >> 2]);
			if (numBytesInPartialGroup == 1) {
				result.append(intToAlpha[(byte0 << 4) & 0x3f]);
				result.append("==");
			} else {
				int byte1 = a[inCursor++] & 0xff;
				result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
				result.append(intToAlpha[(byte1 << 2) & 0x3f]);
				result.append('=');
			}
		}
		return result.toString();
	}

	public byte[] base64ToByteArray(String s) {
		return base64ToByteArray(s, false);
	}

	public byte[] base64ToByteArray(String s, boolean alternate) {
		byte[] alphaToInt = (alternate ? altBase64ToInt : base64ToInt);
		int sLen = s.length();
		int numGroups = sLen / 4;
		if (4 * numGroups != sLen) {
			throw new IllegalArgumentException("String length must be a multiple of four.");
		}
		int missingBytesInLastGroup = 0;
		int numFullGroups = numGroups;
		if (sLen != 0) {
			if (s.charAt(sLen - 1) == '=') {
				missingBytesInLastGroup++;
				numFullGroups--;
			}
			if (s.charAt(sLen - 2) == '=') {
				missingBytesInLastGroup++;
			}
		}
		byte[] result = new byte[3 * numGroups - missingBytesInLastGroup];
		
		int inCursor = 0, outCursor = 0;
		for (int i = 0; i < numFullGroups; i++) {
			int ch0 = base64toInt(s.charAt(inCursor++), alphaToInt);
			int ch1 = base64toInt(s.charAt(inCursor++), alphaToInt);
			int ch2 = base64toInt(s.charAt(inCursor++), alphaToInt);
			int ch3 = base64toInt(s.charAt(inCursor++), alphaToInt);
			result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
			result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
			result[outCursor++] = (byte) ((ch2 << 6) | ch3);
		}

		if (missingBytesInLastGroup != 0) {
			int ch0 = base64toInt(s.charAt(inCursor++), alphaToInt);
			int ch1 = base64toInt(s.charAt(inCursor++), alphaToInt);
			result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
			if (missingBytesInLastGroup == 1) {
				int ch2 = base64toInt(s.charAt(inCursor++), alphaToInt);
				result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
			}
		}
		return result;
	}

	private int base64toInt(char c, byte[] alphaToInt) {
		int result = alphaToInt[c];
		if (result < 0) {
			throw new IllegalArgumentException("Illegal character " + c);
		}
		return result;
	}

}