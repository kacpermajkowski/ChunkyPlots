package pl.kacpermajkowski.ChunkyPlots.config.lang;

public enum Message {
//	Command related messages
	WIDE_HEADER,
	SUBCOMMAND_NOT_FOUND,
	COMMAND_USAGE,
	HELP_COMMAND_ITEM,
	HELP_COMMAND_ERROR,
	SENDER_NOT_PLAYER,
	INSUFFICIENT_PERMISSIONS,

//	Plot related messages
	NULL_PLOT,
	PLOT_CREATED,
	PLOT_DELETED,
	PLOT_ALREADY_EXISTS,

	ENTERED_PLOT,
	LEFT_PLOT,

//	Permission related messages
	NOT_OWNER,
	NOT_PERMITTED,

//	User related messages
	NULL_USER,

//	Member related messages
	ADDED_MEMBER_TO_PLOT,
	ADDED_MEMBER_TO_GROUP,
	REMOVED_MEMBER_FROM_PLOT,
	REMOVED_MEMBER_FROM_GROUP,

	CANNOT_ADD_OWNER_AS_MEMBER,
	PLAYER_IS_ALREADY_A_MEMBER,

//	Blacklist related messages
	BLACKLIST_ADDED_TO_PLOT,
	BLACKLIST_ADDED_TO_GROUP,
	BLACKLIST_REMOVED_FROM_PLOT,
	BLACKLIST_REMOVED_FROM_GROUP,

	CANNOT_ADD_OWNER_TO_BLACKLIST,
	PLAYER_IS_ALREADY_BLACKLISTED,

//	Flag related messages
	NULL_FLAG,

	AVAILABLE_FLAGS,
	DEFAULT_FLAGS,
	PLOT_FLAGS,

	FLAG_SET_ON_PLOT,
	FLAG_SET_ON_GROUP,
	FLAG_VALUE_ON_PLOT,
	FLAG_VALUE_ON_GROUP,
	FLAG_VALUES_IN_GROUP_ARE_DIFFERENT,
	WRONG_FLAG_VALUE,

//  Group related messages
	NULL_GROUP,

	GROUP_CREATE,
	GROUP_ALREADY_EXISTS,

	GROUP_DELETE,
	CANNOT_DELETE_DEFAULT_GROUP,

	PLOT_ADDED_TO_GROUP,
	PLOT_ALREADY_IN_GROUP,
	CANNOT_ADD_PLOT_TO_DEFAULT_GROUP, // Redundant message, can be replaced in code with PLOT_ALREADY_IN_GROUP

	PLOT_REMOVED_FROM_GROUP,
	PLOT_NOT_IN_GROUP,
	CANNOT_REMOVE_PLOT_FROM_DEFAULT_GROUP,

	EMPTY_GROUP,

// 	Visit point related messages
	NULL_VISIT_POINT,

	CREATED_VISIT_POINT,
	VISIT_POINT_NOT_INSIDE_PLOT,
	VISIT_POINT_ALREADY_EXISTS,
	DEFAULT_VISIT_POINT_DESCRIPTION,

	DELETED_VISIT_POINT,
	NOT_VISIT_POINT_OWNER,
	VISIT_POINT_PLOT_DELETED,

	TELEPORTING_TO_VISIT_POINT,
	VISIT_POINT_CLOSED,
	VISIT_POINT_NOT_SAFE,
	ALREADY_TELEPORTING,
	TELEPORT_CANCELLED,
	TELEPORTED_TO_VISIT_POINT,

//	Message for unspecified errors occurring at runtime in code
	ERROR_UNSPECIFIED,
}
