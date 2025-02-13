//
//  CMDWriteLog.java
//  ChatServer
//
//  Created by John McKisson on 5/2/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.awt.Color;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DateFormat;
import java.util.ListIterator;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.presence.chat.*;

import com.lowagie.text.rtf.*;

import static com.presence.chat.ANSIColor.*;

public class CMDWriteLog implements Command {

	public String help() {
		return "";
	}

	public String usage() {
		return "";
	}
	
	static final String TEMPLATE = String.format("%s%s[%s%%-12s%s] %s%%s\n", BLD, WHT, CYN, WHT, RED);

	static final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

	public boolean execute(ChatClient sender, String[] args) {
	
		/*
		ChatLog roomLog = sender.getRoom().getLog();
	
		ListIterator<ChatLogEntry> it = roomLog.entryIterator(roomLog.size());
		
		String FILENAME = "log.rtf";
	
		long startTime = System.currentTimeMillis();
	
		try {
			Document document = new Document();
			RtfWriter2.getInstance(document, new FileOutputStream(FILENAME));
			document.open();
			
			while (it.hasPrevious()) {
				ChatLogEntry entry = it.previous();
				
				document.add(attributify(String.format(TEMPLATE, df.format(entry.getDate()), entry.getMessage())));
			}
			
			document.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		long totalTime = System.currentTimeMillis() - startTime;
		
		sender.sendChat(String.format("%s written: %f seconds", FILENAME, (totalTime / 1000.0f)));
		*/
	
		return true;
	}
		
	int findByte(byte t, byte[] b, int offset) {
		for (int i = offset; i < b.length; i++)
			if (b[i] == t)
				return i;
				
		return -1;
	}
	
	static final int Dim = -1;
	static final int Normal = 0;
	static final int Bright = 1;
	
	static BaseColor[] colors = {BaseColor.BLACK, BaseColor.RED, BaseColor.GREEN, BaseColor.YELLOW, BaseColor.BLUE, BaseColor.MAGENTA,
								new BaseColor(0, 255, 255), BaseColor.BLACK};
	
	Paragraph attributify(String str) {
		Paragraph p = new Paragraph();
		
		//ChatServer.printStringBytes(str);
		
		byte[] b = str.getBytes();
		int escIdx = 0;
		int mIdx = 0;
		int nIdx = 0;
		int len = 0;
	
		
		try {
			//Find escape sequences
			while (mIdx < b.length && nIdx >= mIdx) {
				escIdx = findByte((byte)0x1b, b, nIdx);
				
				//Check for [ char
				if (b[escIdx + 1] == '[') {
				
					//find m
					mIdx = findByte((byte)'m', b, escIdx + 2);
					
					String codeStr = new String(b, escIdx + 2, mIdx - (escIdx + 2));
					String[] codes = codeStr.split(";");
					
					String styleStr = "normal";
					Font font = FontFactory.getFont(FontFactory.COURIER);
					String txt = null;
					
					for (String c : codes) {
					
						int code = Integer.parseInt(c);
					
						nIdx = findByte((byte)0x1b, b, mIdx + 1);
						
						len = nIdx - mIdx - 1;
						
						if (nIdx == -1)
							len = b.length - mIdx - 1;
							
						txt = new String(b, mIdx + 1, len);
						
						switch (code / 10) {
							case 0: case 2: {
								boolean on =  ((int)(code / 10) > 0 ? true : false);
				
								switch (code % 10) {
									case  0: font.setColor(BaseColor.BLACK); break;
									case  1:
										if (on) styleStr += "bold";
										break;
									//case  2:
									//	if(on) styleBrightness = yesNo * Dim; break; // relies on normal brightness == 0
									//case  4: styleUnderline = yesNo; break;
									//case  5: styleBlinking = yesNo; break;
									//case  7: styleInverse = yesNo; break;
								}

								break;
							}
							case 3: font.setColor(colors[code - 30]); break;
							//case 4: styleBackColor = colors[code - 40]; break;
							default:
								break;
						}
					}
					
					font.setStyle(styleStr);

					p.add(new Chunk(txt, font));
					
					//System.out.printf("escIdx %d, code %d, mIdx %d, nIdx %d, len %d, str '%s'\n",
					//	escIdx, code, mIdx, nIdx, len, txt);
				}
			}
		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
			System.out.printf("escIdx %d, mIdx %d, nIdx %d, len %d\n", escIdx, mIdx, nIdx, len);
		}
		
		
		int strIdx = str.indexOf("\u001b[", nIdx);

		
		return p;
	}



/*
- (void)attributify:(NSMutableAttributedString *)astr {
  NSString *sstr = [astr string];
  unsigned int lastMatchLocation = 0, len;
  NSRange foundEscRange;
  BOOL first = YES;
  
  [self attributeDebug:[NSString stringWithFormat:@"entering attributify (%@)", [astr string]]];
  
  while ((foundEscRange = [sstr rangeOfString:@"\x1b[" options:0 range:MWMakeABRange(lastMatchLocation, (len = [sstr length]))]).length) {
    NSRange foundToEndOfStringRange = MWMakeABRange(foundEscRange.location, len);
	//NSLog(@"ansicolorfilter");
    NSRange endEscRange = [sstr rangeOfCharacterFromSet:[NSCharacterSet letterCharacterSet] options:0 range:foundToEndOfStringRange]; 
    NSRange escRange = MWMakeABRange(foundEscRange.location, endEscRange.location + endEscRange.length);
    NSString *endEscChar = nil;

    if (first) {
      first = NO;
      [self applyAttributesToString:astr range:NSMakeRange(0, foundEscRange.location)];
    }

    if (!endEscRange.length) {
      // unterminated sequence, abort!
      return;
    }
    
    endEscChar = [sstr substringWithRange:endEscRange];
    if ([endEscChar isEqual:@"m"]) {
      NSArray *ansiNumberStrings = [[sstr substringWithRange:MWMakeABRange(foundEscRange.location + 2, endEscRange.location)] componentsSeparatedByString:@";"];
      NSEnumerator *e = [ansiNumberStrings objectEnumerator];
      NSString *aString;
      //printf("found escape with %s\n", [[ansiNumberStrings description] cString]);
      
      while ((aString = [e nextObject])) {
        int code = [aString intValue];
        //printf("processing code %i\n", code);
        switch (code / 10) {
          case 0: case 2: {
            BOOL yesNo = !(code / 10);
            //printf("0 or 2 code, yesNo = %i, c%%10 = %i\n", yesNo, code % 10);
            switch (code % 10) {
              case  0: [self resetAttributes]; break;
              case  1: styleBrightness = yesNo * Bright; break;
              case  2: styleBrightness = yesNo * Dim; break; // relies on normal brightness == 0
              case  4: styleUnderline = yesNo; break;
              case  5: styleBlinking = yesNo; break;
              case  7: styleInverse = yesNo; break;
            }
            //printf("after, sBri = %i, sUnd = %i\n", styleBrightness, styleUnderline);
            break;
          }
          case 3: styleForeColor = code - 30; break;
          case 4: styleBackColor = code - 40; break;
          default:
            break;
        }
      }
    }
    
    [self applyAttributesToString:astr range:foundToEndOfStringRange];
    [astr replaceCharactersInRange:escRange withString:@""];
    lastMatchLocation = foundEscRange.location;
  }
*/
}
