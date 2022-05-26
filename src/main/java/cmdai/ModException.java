package cmdai;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

public record ModException(String message) {
	
	public Exception err() {
		return new Exception(message);
	}
	
	public RuntimeException bug() {
		return new RuntimeException("Encountered an impossible bug: " + message);	
	}
	
	public CommandSyntaxException cmd() {
		return new SimpleCommandExceptionType(new LiteralMessage(message)).create();
	}

}
