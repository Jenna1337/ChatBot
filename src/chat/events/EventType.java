package chat.events;

public enum EventType{
	/**
	 * <dt><code>{@link #getEventTypeId()} = 1</code></dt><br/>
	 * Somebody posted a new message
	 */
	MessagePosted(1),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 2</code></dt><br/>
	 * Somebody edited a message
	 */
	MessageEdited(2),
	/**<dt><code>{@link #getEventTypeId()} = 3</code></dt><br/>
	 * Somebody joined the room
	 */
	UserEntered(3),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 4</code></dt><br/>
	 * Somebody left the room
	 */
	UserLeft(4),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 5</code></dt><br/>
	 * The room name or description was changed.
	 */
	RoomNameChanged(5),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 6</code></dt><br/>
	 * A post was starred.
	 */
	MessageStarred(6),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 7</code></dt><br/>
	 * TODO: Figure out what this is
	 */
	DebugMessage(7),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 8</code></dt><br/>
	 * Somebody pinged the user
	 */
	UserMentioned(8),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 9</code></dt><br/>
	 * A post was flagged.
	 * TODO: Verify; Might not be possible.
	 */
	MessageFlagged(9),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 10</code></dt><br/>
	 * Somebody deleted a message.
	 */
	MessageDeleted(10),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 11</code></dt><br/>
	 * TODO: Figure out what this is
	 */
	FileAdded(11),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 12</code></dt><br/>
	 * A post was flagged by a moderator.
	 * TODO: Verify; Might not be possible.
	 */
	ModeratorFlag(12),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 13</code></dt><br/>
	 * Somebody blocked or unblocked somebody.
	 * TODO: Verify
	 */
	UserSettingsChanged(13),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 14</code></dt><br/>
	 * TODO: Figure out what this is
	 */
	GlobalNotification(14),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 15</code></dt><br/>
	 * Somebody changed somebody's access level<br/>
	 * User affected: <code>"target_user_id"</code><br/>
	 * Possible values:<br/>
	 * <ul style="list-style:none">
	 * <li><code>"content":"Access now (default)"</code></li>
	 * <li><code>"content":"Access now read-only"</code></li>
	 * <li><code>"content":"Access now read-write"</code></li>
	 * <li><code>"content":"Access now owner"</code></li><ul>
	 */
	AccessLevelChanged(15),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 16</code></dt><br/>
	 * TODO: Figure out what this is
	 */
	UserNotification(16),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 17</code></dt><br/>
	 * Current user was invited to another room.
	 */
	Invitation(17),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 18</code></dt><br/>
	 * Somebody responded to one of our messages.
	 */
	MessageReply(18),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 19</code></dt><br/>
	 * Messages were moved out of the room.
	 */
	MessageMovedOut(19),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 20</code></dt><br/>
	 * Messages were moved into the room.
	 */
	MessageMovedIn(20),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 21</code></dt><br/>
	 * TODO: Figure out what this is
	 */
	TimeBreak(21),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 22</code></dt><br/>
	 * The ticker feed added new feed items.
	 */
	FeedTicker(22),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 29</code></dt><br/>
	 * TODO: Figure out what this is
	 */
	UserSuspended(29),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 30</code></dt><br/>
	 * TODO: Figure out what this is
	 */
	UserMerged(30),
	/**
	 * <dt><code>{@link #getEventTypeId()} = 34</code></dt><br/>
	 * Somebody changed their avatar or name.
	 * The user that changed their avatar or name is obtainable via the
	 * {@link ChatEvent#getTargetUserId()} method.
	 */
	UserNameOrAvatarChanged(34)
	;
	final int id;
	EventType(int id){
		this.id=id;
	}
	public int getEventTypeId(){
		return this.id;
	}
	public static EventType forEventId(long l){
		for(EventType evt : EventType.values())
			if(l==evt.getEventTypeId())
				return evt;
		return null;
	}
}
